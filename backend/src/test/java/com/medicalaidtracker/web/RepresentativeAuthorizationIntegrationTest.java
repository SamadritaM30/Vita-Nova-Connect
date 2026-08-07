package com.medicalaidtracker.web;

import com.medicalaidtracker.appointment.Appointment;
import com.medicalaidtracker.appointment.AppointmentRepository;
import com.medicalaidtracker.common.domain.DomainEnums.AuthProvider;
import com.medicalaidtracker.common.domain.DomainEnums.AppointmentStatus;
import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.common.domain.DomainEnums.UserStatus;
import com.medicalaidtracker.doctor.DoctorRepository;
import com.medicalaidtracker.hospital.HospitalRepository;
import com.medicalaidtracker.representative.RepresentativeHospital;
import com.medicalaidtracker.representative.RepresentativeHospitalRepository;
import com.medicalaidtracker.user.PatientProfile;
import com.medicalaidtracker.user.PatientProfileRepository;
import com.medicalaidtracker.user.RepresentativeProfile;
import com.medicalaidtracker.user.RepresentativeProfileRepository;
import com.medicalaidtracker.user.User;
import com.medicalaidtracker.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RepresentativeAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepresentativeProfileRepository profileRepository;

    @Autowired
    private RepresentativeHospitalRepository assignmentRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientProfileRepository patientProfileRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @BeforeEach
    void createRepresentativeAssignedOnlyToHospitalOne() {
        var user = new User();
        user.setEmail("scoped.rep@example.com");
        user.setDisplayName("Scoped Representative");
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderSubject("scoped-rep-sub");
        user.setRole(Role.REPRESENTATIVE);
        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        var profile = new RepresentativeProfile();
        profile.setUser(user);
        profile.setEmployeeId("REP-TEST");
        profile.setOrganizationName("Test Hospital");
        profile = profileRepository.save(profile);

        var assignment = new RepresentativeHospital();
        assignment.setRepresentativeProfile(profile);
        assignment.setHospital(hospitalRepository.findById(1L).orElseThrow());
        assignmentRepository.save(assignment);
    }

    @Test
    void representativeCanUpdateAssignedHospitalDoctor() throws Exception {
        mockMvc.perform(patch("/api/v1/representative/doctors/1/availability")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availabilityStatus\":\"UNAVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availabilityStatus").value("UNAVAILABLE"));
    }

    @Test
    void representativeCannotUpdateAnotherHospitalsDoctor() throws Exception {
        mockMvc.perform(patch("/api/v1/representative/doctors/3/availability")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availabilityStatus\":\"UNAVAILABLE\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void dashboardAndAppointmentListAreRestrictedToAssignedHospitals() throws Exception {
        mockMvc.perform(get("/api/v1/representative/dashboard")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.availableIcuBeds").isNumber())
                .andExpect(jsonPath("$.data.availableGeneralBeds").isNumber())
                .andExpect(jsonPath("$.data.todayAppointments").isNumber())
                .andExpect(jsonPath("$.data.assignedHospitals[0].id").value(1));

        mockMvc.perform(get("/api/v1/representative/appointments")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void representativeAppointmentFiltersUseEffectiveCompletedStatus() throws Exception {
        var patientUser = new User();
        patientUser.setEmail("representative-filter.patient@example.com");
        patientUser.setDisplayName("Representative Filter Patient");
        patientUser.setProvider(AuthProvider.GOOGLE);
        patientUser.setProviderSubject("representative-filter-patient-sub");
        patientUser.setRole(Role.PATIENT);
        patientUser.setStatus(UserStatus.ACTIVE);
        patientUser = userRepository.save(patientUser);

        var patient = new PatientProfile();
        patient.setUser(patientUser);
        patient.setDateOfBirth(LocalDate.of(2001, 1, 1));
        patient = patientProfileRepository.save(patient);

        var doctor = doctorRepository.findById(1L).orElseThrow();
        var elapsedAppointment = new Appointment();
        elapsedAppointment.setPatient(patient);
        elapsedAppointment.setDoctor(doctor);
        elapsedAppointment.setHospital(doctor.getHospital());
        elapsedAppointment.setStartsAt(Instant.now().minus(2, ChronoUnit.HOURS));
        elapsedAppointment.setEndsAt(Instant.now().minus(1, ChronoUnit.HOURS));
        elapsedAppointment.setStatus(AppointmentStatus.BOOKED);
        elapsedAppointment = appointmentRepository.saveAndFlush(elapsedAppointment);

        var cancelledAppointment = new Appointment();
        cancelledAppointment.setPatient(patient);
        cancelledAppointment.setDoctor(doctor);
        cancelledAppointment.setHospital(doctor.getHospital());
        cancelledAppointment.setStartsAt(Instant.now().plus(2, ChronoUnit.HOURS));
        cancelledAppointment.setEndsAt(Instant.now().plus(3, ChronoUnit.HOURS));
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(cancelledAppointment);

        mockMvc.perform(get("/api/v1/representative/dashboard")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.todayAppointments").value(1));

        mockMvc.perform(get("/api/v1/representative/appointments")
                        .param("status", "COMPLETED")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id")
                        .value(elapsedAppointment.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].status").value("COMPLETED"));

        mockMvc.perform(get("/api/v1/representative/appointments")
                        .param("status", "BOOKED")
                        .with(user("scoped.rep@example.com").roles("REPRESENTATIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
