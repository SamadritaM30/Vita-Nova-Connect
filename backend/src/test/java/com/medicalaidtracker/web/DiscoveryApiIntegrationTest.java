package com.medicalaidtracker.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DiscoveryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void doctorSearchCombinesSpecializationAndBedAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/doctors")
                        .param("specialization", "Cardiologist")
                        .param("availabilityStatus", "AVAILABLE")
                        .param("wardType", "ICU")
                        .with(user("patient@example.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].specialization").value("Cardiologist"));
    }

    @Test
    void emergencySearchOnlyReturnsHospitalsWithBedsAndDoctors() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/emergency-availability")
                        .param("wardType", "ICU")
                        .with(user("patient@example.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].availableIcuBeds").isNumber())
                .andExpect(jsonPath("$.data.content[0].availableDoctors").isNumber());
    }

    @Test
    void unsupportedSortFieldReturnsSafeBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/doctors")
                        .param("sort", "hospital.name,asc")
                        .with(user("patient@example.com").roles("PATIENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT"))
                .andExpect(jsonPath("$.detail")
                        .value("Unsupported sort field: hospital.name"));
    }

    @Test
    void invalidEnumReturnsGenericBadRequestWithoutJavaDetails() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/emergency-availability")
                        .param("wardType", "NOT_A_WARD")
                        .with(user("patient@example.com").roles("PATIENT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail")
                        .value("A request parameter is missing or has an invalid value."));
    }
}
