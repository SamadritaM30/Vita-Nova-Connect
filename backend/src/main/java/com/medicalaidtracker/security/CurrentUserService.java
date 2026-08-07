package com.medicalaidtracker.security;

import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.common.domain.DomainEnums.UserStatus;
import com.medicalaidtracker.common.exception.ResourceNotFoundException;
import com.medicalaidtracker.representative.RepresentativeHospitalRepository;
import com.medicalaidtracker.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final RepresentativeProfileRepository representativeProfileRepository;
    private final RepresentativeHospitalRepository representativeHospitalRepository;

    @Transactional(readOnly = true)
    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required.");
        }
        String identity = authentication.getPrincipal() instanceof OAuth2User oauth2User
                ? oauth2User.getAttribute("email")
                : authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(identity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AUTHENTICATED_USER_NOT_FOUND", "The authenticated user no longer exists."));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("This account is not active.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public PatientProfile requirePatientProfile() {
        User user = requireUser();
        if (user.getRole() != Role.PATIENT) {
            throw new AccessDeniedException("A patient account is required.");
        }
        return patientProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PATIENT_PROFILE_NOT_FOUND", "Complete your patient profile before continuing."));
    }

    @Transactional(readOnly = true)
    public RepresentativeProfile requireRepresentativeProfile() {
        User user = requireUser();
        if (user.getRole() != Role.REPRESENTATIVE) {
            throw new AccessDeniedException("A representative account is required.");
        }
        return representativeProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "REPRESENTATIVE_PROFILE_NOT_FOUND", "Representative profile not found."));
    }

    @Transactional(readOnly = true)
    public List<Long> representativeHospitalIds() {
        return representativeHospitalRepository.findHospitalIdsByRepresentativeProfileId(
                requireRepresentativeProfile().getId());
    }

    @Transactional(readOnly = true)
    public void requireRepresentativeHospital(Long hospitalId) {
        var profile = requireRepresentativeProfile();
        if (!representativeHospitalRepository.existsByRepresentativeProfileIdAndHospitalId(
                profile.getId(), hospitalId)) {
            throw new AccessDeniedException("You are not assigned to this hospital.");
        }
    }
}
