package com.medicalaidtracker.appointment;

import com.medicalaidtracker.common.domain.DomainEnums.AuthProvider;
import com.medicalaidtracker.common.domain.DomainEnums.AppointmentStatus;
import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.common.domain.DomainEnums.UserStatus;
import com.medicalaidtracker.config.AppProperties;
import com.medicalaidtracker.doctor.DoctorRepository;
import com.medicalaidtracker.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private AppProperties properties;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void createPatients() {
        createPatient("booking.patient@example.com", "Booking Patient", "booking-patient-sub");
        createPatient("other.patient@example.com", "Other Patient", "other-patient-sub");
    }

    @Test
    void bookingIsOwnedByCurrentPatientAndCancellationKeepsHistory() throws Exception {
        String startsAt = nextDoctorOneSlot();
        String createJson = """
                {"doctorId":1,"startsAt":"%s"}
                """.formatted(startsAt);

        String appointmentId = com.jayway.jsonpath.JsonPath.read(
                mockMvc.perform(post("/api/v1/appointments")
                                .with(user("booking.patient@example.com").roles("PATIENT"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.id", notNullValue()))
                        .andExpect(jsonPath("$.data.status").value("BOOKED"))
                        .andReturn().getResponse().getContentAsString(),
                "$.data.id"
        );

        mockMvc.perform(patch("/api/v1/appointments/{id}/cancel", appointmentId)
                        .with(user("other.patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Not my appointment\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/appointments/{id}/cancel", appointmentId)
                        .with(user("booking.patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Schedule changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Schedule changed"));
    }

    @Test
    void overlappingActiveDoctorSlotIsRejected() throws Exception {
        String createJson = """
                {"doctorId":1,"startsAt":"%s"}
                """.formatted(nextDoctorOneSlot());

        mockMvc.perform(post("/api/v1/appointments")
                        .with(user("booking.patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/appointments")
                        .with(user("other.patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_SLOT_UNAVAILABLE"));
    }

    @Test
    void patientCanCreateAmbulanceRequestWithoutSupplyingAPatientId() throws Exception {
        mockMvc.perform(post("/api/v1/ambulance-requests")
                        .with(user("booking.patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hospitalId": 1,
                                  "pickupAddress": "Paltan Bazar, Guwahati",
                                  "contactNumber": "9876543210"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.hospital.id").value(1));
    }

    @Test
    void ambulanceRequestHistoryOnlyContainsCurrentPatientsRequests() throws Exception {
        createAmbulanceRequest(
                "booking.patient@example.com",
                "Own pickup address",
                "9876543210"
        );
        createAmbulanceRequest(
                "other.patient@example.com",
                "Another patient's pickup address",
                "9876543211"
        );

        mockMvc.perform(get("/api/v1/ambulance-requests")
                        .with(user("booking.patient@example.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].pickupAddress").value("Own pickup address"));
    }

    @Test
    void elapsedAppointmentCannotBeCancelledOrRewritten() throws Exception {
        var patientUser = userRepository.findByEmailIgnoreCase("booking.patient@example.com")
                .orElseThrow();
        var patient = patientProfileRepository.findByUserId(patientUser.getId()).orElseThrow();
        var doctor = doctorRepository.findDetailedById(1L).orElseThrow();
        var appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setHospital(doctor.getHospital());
        appointment.setStartsAt(Instant.now().minus(2, ChronoUnit.HOURS));
        appointment.setEndsAt(Instant.now().minus(1, ChronoUnit.HOURS));
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment = appointmentRepository.saveAndFlush(appointment);

        mockMvc.perform(patch("/api/v1/appointments/{id}/cancel", appointment.getId())
                        .with(user("booking.patient@example.com").roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Too late to cancel\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_ALREADY_STARTED"));

        assertThat(appointmentRepository.findById(appointment.getId()).orElseThrow().getStatus())
                .isEqualTo(AppointmentStatus.BOOKED);

        mockMvc.perform(get("/api/v1/appointments")
                        .param("status", "COMPLETED")
                        .with(user("booking.patient@example.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(appointment.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].status").value("COMPLETED"));

        mockMvc.perform(get("/api/v1/appointments")
                        .param("status", "BOOKED")
                        .with(user("booking.patient@example.com").roles("PATIENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    private void createPatient(String email, String displayName, String subject) {
        var user = new User();
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderSubject(subject);
        user.setRole(Role.PATIENT);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        var profile = new PatientProfile();
        profile.setUser(user);
        profile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        profile.setContactNumber("9876543210");
        profile.setBloodGroup("O+");
        patientProfileRepository.save(profile);
    }

    private String nextDoctorOneSlot() {
        return ZonedDateTime.of(
                        LocalDate.now(properties.timeZone()).plusDays(7),
                        doctorRepository.findById(1L).orElseThrow().getConsultationFrom(),
                        properties.timeZone())
                .toOffsetDateTime()
                .toString();
    }

    private void createAmbulanceRequest(String email, String pickupAddress, String contactNumber)
            throws Exception {
        mockMvc.perform(post("/api/v1/ambulance-requests")
                        .with(user(email).roles("PATIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hospitalId": 1,
                                  "pickupAddress": "%s",
                                  "contactNumber": "%s"
                                }
                                """.formatted(pickupAddress, contactNumber)))
                .andExpect(status().isCreated());
    }
}
