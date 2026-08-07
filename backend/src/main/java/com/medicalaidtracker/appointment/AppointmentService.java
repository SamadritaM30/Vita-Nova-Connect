package com.medicalaidtracker.appointment;

import com.medicalaidtracker.audit.AuditService;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.AppointmentStatus;
import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.common.exception.BusinessRuleException;
import com.medicalaidtracker.common.exception.ResourceNotFoundException;
import com.medicalaidtracker.config.AppProperties;
import com.medicalaidtracker.doctor.Doctor;
import com.medicalaidtracker.doctor.DoctorDtos.AppointmentSlotResponse;
import com.medicalaidtracker.doctor.DoctorRepository;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import com.medicalaidtracker.security.CurrentUserService;
import com.medicalaidtracker.user.PatientProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.medicalaidtracker.appointment.AppointmentDtos.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> availableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findDetailedById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DOCTOR_NOT_FOUND", "Doctor not found."));
        if (doctor.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE
                || date.isBefore(LocalDate.now(properties.timeZone()))) {
            return List.of();
        }

        ZoneId zone = properties.timeZone();
        Instant from = date.atStartOfDay(zone).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(zone).toInstant();
        var occupiedStarts = new HashSet<>(appointmentRepository.findDoctorSchedule(
                        doctorId, AppointmentStatus.BOOKED, from, to)
                .stream()
                .map(Appointment::getStartsAt)
                .toList());

        int duration = properties.appointmentDurationMinutes();
        ZonedDateTime cursor = ZonedDateTime.of(date, doctor.getConsultationFrom(), zone);
        ZonedDateTime workEnds = ZonedDateTime.of(date, doctor.getConsultationTo(), zone);
        Instant now = Instant.now();
        var slots = new java.util.ArrayList<AppointmentSlotResponse>();
        while (!cursor.plusMinutes(duration).isAfter(workEnds)) {
            Instant slotStart = cursor.toInstant();
            if (slotStart.isAfter(now) && !occupiedStarts.contains(slotStart)) {
                slots.add(new AppointmentSlotResponse(
                        cursor.toOffsetDateTime(),
                        cursor.plusMinutes(duration).toOffsetDateTime()
                ));
            }
            cursor = cursor.plusMinutes(duration);
        }
        return List.copyOf(slots);
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> listPatientAppointments(
            AppointmentStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            OffsetDateTime startsAfter,
            Pageable pageable
    ) {
        PatientProfile patient = currentUserService.requirePatientProfile();
        Instant from = dateFrom == null
                ? null
                : dateFrom.atStartOfDay(properties.timeZone()).toInstant();
        if (startsAfter != null && (from == null || startsAfter.toInstant().isAfter(from))) {
            from = startsAfter.toInstant();
        }
        Instant to = dateTo == null
                ? null
                : dateTo.plusDays(1).atStartOfDay(properties.timeZone()).toInstant();
        return PageResponse.from(
                appointmentRepository.findAllForPatient(
                        patient.getId(),
                        status,
                        AppointmentStatus.BOOKED,
                        AppointmentStatus.COMPLETED,
                        AppointmentStatus.CANCELLED,
                        Instant.now(),
                        from,
                        to,
                        pageable),
                this::map
        );
    }

    @Transactional
    public AppointmentResponse book(AppointmentCreateRequest request) {
        PatientProfile patient = currentUserService.requirePatientProfile();
        requireCompleteProfile(patient);

        // Locking the doctor makes overlap-check + insert atomic across concurrent requests.
        Doctor doctor = doctorRepository.findByIdForUpdate(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DOCTOR_NOT_FOUND", "Doctor not found."));
        if (doctor.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new BusinessRuleException(
                    "DOCTOR_UNAVAILABLE", "This doctor is currently unavailable.");
        }

        Instant startsAt = request.startsAt().toInstant().truncatedTo(ChronoUnit.SECONDS);
        Instant endsAt = startsAt.plus(properties.appointmentDurationMinutes(), ChronoUnit.MINUTES);
        validateWorkingHours(doctor, startsAt, endsAt);
        if (appointmentRepository.existsOverlapping(
                doctor.getId(), startsAt, endsAt, AppointmentStatus.BOOKED)) {
            throw new BusinessRuleException(
                    "APPOINTMENT_SLOT_UNAVAILABLE", "The selected appointment slot is no longer available.");
        }

        var appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setHospital(doctor.getHospital());
        appointment.setStartsAt(startsAt);
        appointment.setEndsAt(endsAt);
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment = appointmentRepository.save(appointment);

        auditService.record(
                patient.getUser(),
                "APPOINTMENT_BOOKED",
                "Appointment",
                appointment.getId(),
                "doctorId=" + doctor.getId() + ", startsAt=" + startsAt
        );
        return map(appointment);
    }

    @Transactional
    public AppointmentResponse cancelPatientAppointment(UUID id, AppointmentCancelRequest request) {
        PatientProfile patient = currentUserService.requirePatientProfile();
        Appointment appointment = appointmentRepository.findByIdAndPatientId(id, patient.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APPOINTMENT_NOT_FOUND", "Appointment not found."));
        cancel(appointment, request.reason());
        auditService.record(
                patient.getUser(),
                "APPOINTMENT_CANCELLED_BY_PATIENT",
                "Appointment",
                appointment.getId(),
                "reason=" + request.reason().trim()
        );
        return map(appointment);
    }

    @Transactional
    public AppointmentResponse cancelRepresentativeAppointment(
            Appointment appointment,
            String reason
    ) {
        cancel(appointment, reason);
        var actor = currentUserService.requireUser();
        auditService.record(
                actor,
                "APPOINTMENT_CANCELLED_BY_REPRESENTATIVE",
                "Appointment",
                appointment.getId(),
                "reason=" + reason.trim()
        );
        return map(appointment);
    }

    public AppointmentResponse map(Appointment appointment) {
        var doctor = appointment.getDoctor();
        var hospital = appointment.getHospital();
        var patient = appointment.getPatient();
        AppointmentStatus effectiveStatus =
                appointment.getStatus() == AppointmentStatus.BOOKED
                        && !appointment.getEndsAt().isAfter(Instant.now())
                        ? AppointmentStatus.COMPLETED
                        : appointment.getStatus();
        return new AppointmentResponse(
                appointment.getId(),
                effectiveStatus,
                appointment.getStartsAt().atZone(properties.timeZone()).toOffsetDateTime(),
                appointment.getEndsAt().atZone(properties.timeZone()).toOffsetDateTime(),
                appointment.getCancellationReason(),
                new DoctorSummary(doctor.getId(), doctor.getName(), doctor.getSpecialization()),
                new HospitalSummary(
                        hospital.getId(), hospital.getName(), hospital.getAddress(), hospital.getContactNumber()),
                new PatientSummary(
                        patient.getId(), patient.getUser().getDisplayName(), patient.getContactNumber())
        );
    }

    private void cancel(Appointment appointment, String reason) {
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BusinessRuleException(
                    "APPOINTMENT_NOT_CANCELLABLE", "Only booked appointments can be cancelled.");
        }
        if (!appointment.getStartsAt().isAfter(Instant.now())) {
            throw new BusinessRuleException(
                    "APPOINTMENT_ALREADY_STARTED",
                    "An appointment that has already started cannot be cancelled."
            );
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason.trim());
        appointment.setCancelledAt(Instant.now());
        appointmentRepository.save(appointment);
    }

    private void validateWorkingHours(Doctor doctor, Instant startsAt, Instant endsAt) {
        if (!startsAt.isAfter(Instant.now())) {
            throw new BusinessRuleException(
                    "APPOINTMENT_IN_PAST",
                    "Appointment start time must be in the future.",
                    HttpStatus.BAD_REQUEST
            );
        }

        ZoneId zone = properties.timeZone();
        LocalDateTime localStart = LocalDateTime.ofInstant(startsAt, zone);
        LocalDateTime localEnd = LocalDateTime.ofInstant(endsAt, zone);
        if (!localStart.toLocalDate().equals(localEnd.toLocalDate())
                || localStart.toLocalTime().isBefore(doctor.getConsultationFrom())
                || localEnd.toLocalTime().isAfter(doctor.getConsultationTo())) {
            throw new BusinessRuleException(
                    "OUTSIDE_WORKING_HOURS",
                    "The selected slot is outside the doctor's consultation hours.",
                    HttpStatus.BAD_REQUEST
            );
        }

        long offsetMinutes = Duration.between(
                doctor.getConsultationFrom(), localStart.toLocalTime()).toMinutes();
        if (localStart.getSecond() != 0 || localStart.getNano() != 0
                || offsetMinutes % properties.appointmentDurationMinutes() != 0) {
            throw new BusinessRuleException(
                    "INVALID_APPOINTMENT_SLOT",
                    "Select one of the published appointment slots.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void requireCompleteProfile(PatientProfile profile) {
        if (profile.getDateOfBirth() == null
                || profile.getContactNumber() == null
                || profile.getBloodGroup() == null) {
            throw new BusinessRuleException(
                    "PROFILE_INCOMPLETE",
                    "Complete your patient profile before booking an appointment.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
