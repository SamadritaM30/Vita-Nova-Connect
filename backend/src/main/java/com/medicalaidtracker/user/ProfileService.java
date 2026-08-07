package com.medicalaidtracker.user;

import com.medicalaidtracker.audit.AuditService;
import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.hospital.HospitalRepository;
import com.medicalaidtracker.representative.RepresentativeHospitalRepository;
import com.medicalaidtracker.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.medicalaidtracker.user.ProfileDtos.*;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final PatientProfileRepository patientProfileRepository;
    private final RepresentativeProfileRepository representativeProfileRepository;
    private final RepresentativeHospitalRepository representativeHospitalRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentProfile() {
        return map(currentUserService.requireUser());
    }

    @Transactional
    public ProfileResponse updateCurrentProfile(ProfileUpdateRequest request) {
        User user = currentUserService.requireUser();
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().trim());
            userRepository.save(user);
        }

        if (user.getRole() == Role.PATIENT) {
            PatientProfile profile = patientProfileRepository.findByUserId(user.getId()).orElseThrow();
            if (request.dateOfBirth() != null) profile.setDateOfBirth(request.dateOfBirth());
            if (request.contactNumber() != null) profile.setContactNumber(request.contactNumber().trim());
            if (request.bloodGroup() != null) profile.setBloodGroup(request.bloodGroup());
            if (request.address() != null) profile.setAddress(trimToNull(request.address()));
            patientProfileRepository.save(profile);
        } else {
            RepresentativeProfile profile = representativeProfileRepository.findByUserId(user.getId()).orElseThrow();
            if (request.contactNumber() != null) profile.setContactNumber(request.contactNumber().trim());
            if (request.employeeId() != null) profile.setEmployeeId(trimToNull(request.employeeId()));
            if (request.organizationName() != null) {
                profile.setOrganizationName(trimToNull(request.organizationName()));
            }
            representativeProfileRepository.save(profile);
        }

        auditService.record(user, "PROFILE_UPDATED", "User", user.getId(), "Own profile updated");
        return map(user);
    }

    @Transactional(readOnly = true)
    public boolean isProfileComplete(User user) {
        if (user.getRole() == Role.PATIENT) {
            return patientProfileRepository.findByUserId(user.getId())
                    .map(p -> p.getDateOfBirth() != null
                            && p.getContactNumber() != null
                            && p.getBloodGroup() != null)
                    .orElse(false);
        }
        return representativeProfileRepository.findByUserId(user.getId())
                .map(p -> p.getEmployeeId() != null && p.getOrganizationName() != null)
                .orElse(false);
    }

    private ProfileResponse map(User user) {
        if (user.getRole() == Role.PATIENT) {
            PatientProfile profile = patientProfileRepository.findByUserId(user.getId()).orElseThrow();
            boolean complete = profile.getDateOfBirth() != null
                    && profile.getContactNumber() != null
                    && profile.getBloodGroup() != null;
            return new ProfileResponse(
                    user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.getRole(),
                    profile.getDateOfBirth(), profile.getContactNumber(), profile.getBloodGroup(),
                    profile.getAddress(), null, null, List.of(), complete
            );
        }

        RepresentativeProfile profile = representativeProfileRepository.findByUserId(user.getId()).orElseThrow();
        List<Long> hospitalIds = representativeHospitalRepository
                .findHospitalIdsByRepresentativeProfileId(profile.getId());
        List<HospitalAssignment> assignments = hospitalRepository.findAllById(hospitalIds).stream()
                .map(h -> new HospitalAssignment(h.getId(), h.getName()))
                .toList();
        boolean complete = profile.getEmployeeId() != null && profile.getOrganizationName() != null;
        return new ProfileResponse(
                user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl(), user.getRole(),
                null, profile.getContactNumber(), null, null,
                profile.getEmployeeId(), profile.getOrganizationName(), assignments, complete
        );
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
