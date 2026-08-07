package com.medicalaidtracker.security;

import com.medicalaidtracker.MedicalAidTrackerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(classes = MedicalAidTrackerApplication.class)
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedApiReturnsProblemDetailWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/doctors"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @WithMockUser(username = "patient@example.com", roles = "PATIENT")
    void patientCannotAccessRepresentativeEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/representative/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void csrfEndpointReturnsHeaderAndTokenForAuthenticatedSpa() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf")
                        .with(user("patient@example.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void stateChangingRequestWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(patch("/api/v1/profile")
                        .with(user("patient@example.com").roles("PATIENT"))
                        .contentType("application/json")
                        .content("{\"displayName\":\"Patient Example\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void corsPreflightAllowsSessionCsrfHeader() throws Exception {
        mockMvc.perform(options("/api/v1/profile")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "content-type,x-csrf-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsStringIgnoringCase("X-CSRF-TOKEN")));
    }

    @Test
    void malformedJsonReturnsSafeProblemDetail() throws Exception {
        mockMvc.perform(patch("/api/v1/profile")
                        .with(user("patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.detail")
                        .value("The request body is malformed or contains an unsupported value."));
    }

    @Test
    void actuatorMetricsAreNotAvailableToApplicationRoles() throws Exception {
        mockMvc.perform(get("/actuator/metrics")
                        .with(user("patient@example.com").roles("PATIENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
