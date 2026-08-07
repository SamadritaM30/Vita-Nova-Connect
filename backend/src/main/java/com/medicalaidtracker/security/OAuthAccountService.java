package com.medicalaidtracker.security;

import com.medicalaidtracker.audit.AuditService;
import com.medicalaidtracker.common.domain.DomainEnums.AuthProvider;
import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.common.domain.DomainEnums.UserStatus;
import com.medicalaidtracker.representative.RepresentativeHospital;
import com.medicalaidtracker.representative.RepresentativeHospitalRepository;
import com.medicalaidtracker.representative.RepresentativeInvitationRepository;
import com.medicalaidtracker.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final RepresentativeProfileRepository representativeProfileRepository;
    private final RepresentativeInvitationRepository invitationRepository;
    private final RepresentativeHospitalRepository representativeHospitalRepository;
    private final AuditService auditService;

    @Transactional
    public User provisionGoogleUser(
            String email,
            String name,
            String avatarUrl,
            String subject,
            boolean emailVerified
    ) {
        if (email == null || email.isBlank() || subject == null || subject.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_google_profile"),
                    "Google did not return a usable email and subject.");
        }
        if (!emailVerified) {
            throw new OAuth2AuthenticationException(new OAuth2Error("email_not_verified"),
                    "A verified Google email is required.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        var invitations = invitationRepository.findAllByEmailIgnoreCaseAndActiveTrue(normalizedEmail);

        User user = userRepository.findByProviderAndProviderSubject(AuthProvider.GOOGLE, subject)
                .map(existing -> {
                    userRepository.findByEmailIgnoreCase(normalizedEmail)
                            .filter(emailOwner -> !emailOwner.getId().equals(existing.getId()))
                            .ifPresent(emailOwner -> {
                                throw accountConflict();
                            });
                    return existing;
                })
                .orElseGet(() -> {
                    if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
                        throw accountConflict();
                    }
                    return new User();
                });

        boolean newUser = user.getId() == null;
        Role previousRole = user.getRole();
        user.setEmail(normalizedEmail);
        user.setDisplayName((name == null || name.isBlank()) ? normalizedEmail : name.trim());
        user.setAvatarUrl(avatarUrl);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderSubject(subject);
        if (newUser) {
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(invitations.isEmpty() ? Role.PATIENT : Role.REPRESENTATIVE);
        } else if (!invitations.isEmpty()) {
            user.setRole(Role.REPRESENTATIVE);
        }
        user = userRepository.save(user);
        if (newUser) {
            auditService.record(
                    user,
                    "USER_PROVISIONED",
                    "User",
                    user.getId(),
                    "provider=GOOGLE, role=" + user.getRole()
            );
        } else if (previousRole != Role.REPRESENTATIVE && user.getRole() == Role.REPRESENTATIVE) {
            auditService.record(
                    user,
                    "USER_ROLE_ASSIGNED",
                    "User",
                    user.getId(),
                    "role=REPRESENTATIVE, source=invitation"
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_suspended"),
                    "This account has been suspended.");
        }

        if (user.getRole() == Role.PATIENT) {
            ensurePatientProfile(user);
        } else {
            var profile = ensureRepresentativeProfile(user);
            for (var invitation : invitations) {
                if (!representativeHospitalRepository.existsByRepresentativeProfileIdAndHospitalId(
                        profile.getId(), invitation.getHospital().getId())) {
                    var assignment = new RepresentativeHospital();
                    assignment.setRepresentativeProfile(profile);
                    assignment.setHospital(invitation.getHospital());
                    assignment = representativeHospitalRepository.save(assignment);
                    auditService.record(
                            user,
                            "REPRESENTATIVE_HOSPITAL_ASSIGNED",
                            "RepresentativeHospital",
                            assignment.getId(),
                            "hospitalId=" + invitation.getHospital().getId()
                    );
                }
                if (profile.getEmployeeId() == null) {
                    profile.setEmployeeId(invitation.getEmployeeId());
                }
                if (profile.getOrganizationName() == null) {
                    profile.setOrganizationName(invitation.getOrganizationName());
                }
                invitation.setAcceptedAt(Instant.now());
                invitation.setActive(false);
                auditService.record(
                        user,
                        "REPRESENTATIVE_INVITATION_ACCEPTED",
                        "RepresentativeInvitation",
                        invitation.getId(),
                        "hospitalId=" + invitation.getHospital().getId()
                );
            }
            representativeProfileRepository.save(profile);
        }
        return user;
    }

    private OAuth2AuthenticationException accountConflict() {
        return new OAuth2AuthenticationException(
                new OAuth2Error("oauth_account_conflict"),
                "An account already exists for this email with a different Google identity."
        );
    }

    private void ensurePatientProfile(User user) {
        if (patientProfileRepository.findByUserId(user.getId()).isEmpty()) {
            var profile = new PatientProfile();
            profile.setUser(user);
            patientProfileRepository.save(profile);
        }
    }

    private RepresentativeProfile ensureRepresentativeProfile(User user) {
        return representativeProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
            var profile = new RepresentativeProfile();
            profile.setUser(user);
            return representativeProfileRepository.save(profile);
        });
    }
}
