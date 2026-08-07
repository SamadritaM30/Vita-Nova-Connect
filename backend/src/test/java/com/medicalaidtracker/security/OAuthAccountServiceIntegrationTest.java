package com.medicalaidtracker.security;

import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.representative.RepresentativeHospitalRepository;
import com.medicalaidtracker.user.PatientProfileRepository;
import com.medicalaidtracker.user.RepresentativeProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class OAuthAccountServiceIntegrationTest {

    @Autowired
    private OAuthAccountService accountService;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private RepresentativeProfileRepository representativeProfileRepository;

    @Autowired
    private RepresentativeHospitalRepository representativeHospitalRepository;

    @Autowired
    private OAuthGrantedAuthoritiesMapper authoritiesMapper;

    @Test
    void newGoogleUserBecomesPatientAndGetsAProfile() {
        var user = accountService.provisionGoogleUser(
                "new.patient@example.com", "New Patient", null, "google-sub-patient-test", true);

        assertThat(user.getRole()).isEqualTo(Role.PATIENT);
        assertThat(patientProfileRepository.findByUserId(user.getId())).isPresent();
    }

    @Test
    void representativeRoleOnlyComesFromServerSideInvitation() {
        var user = accountService.provisionGoogleUser(
                "representative@medicalaid.demo",
                "Hospital Representative",
                null,
                "google-sub-representative-test",
                true
        );

        assertThat(user.getRole()).isEqualTo(Role.REPRESENTATIVE);
        var profile = representativeProfileRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(representativeHospitalRepository
                .existsByRepresentativeProfileIdAndHospitalId(profile.getId(), 1L)).isTrue();
    }

    @Test
    void existingEmailCannotBeRelinkedToDifferentGoogleSubject() {
        var original = accountService.provisionGoogleUser(
                "stable.identity@example.com",
                "Stable Identity",
                null,
                "google-sub-original",
                true
        );

        assertThatThrownBy(() -> accountService.provisionGoogleUser(
                "stable.identity@example.com",
                "Impersonating Identity",
                null,
                "google-sub-different",
                true
        ))
                .isInstanceOfSatisfying(
                        OAuth2AuthenticationException.class,
                        ex -> assertThat(ex.getError().getErrorCode())
                                .isEqualTo("oauth_account_conflict")
                );

        assertThat(original.getProviderSubject()).isEqualTo("google-sub-original");
    }

    @Test
    void missingEmailVerifiedClaimIsRejectedBeforeProvisioning() {
        var unverifiedAuthority = new OAuth2UserAuthority(Map.of(
                "email", "missing.claim@example.com",
                "name", "Missing Claim",
                "sub", "google-sub-missing-claim"
        ));

        assertThatThrownBy(() -> authoritiesMapper.mapAuthorities(List.of(unverifiedAuthority)))
                .isInstanceOfSatisfying(
                        OAuth2AuthenticationException.class,
                        ex -> assertThat(ex.getError().getErrorCode())
                                .isEqualTo("email_not_verified")
                );
    }
}
