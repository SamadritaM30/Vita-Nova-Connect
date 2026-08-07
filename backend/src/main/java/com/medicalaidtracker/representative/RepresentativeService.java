package com.medicalaidtracker.representative;

import com.medicalaidtracker.appointment.Appointment;
import com.medicalaidtracker.appointment.AppointmentDtos.AppointmentCancelRequest;
import com.medicalaidtracker.appointment.AppointmentDtos.AppointmentResponse;
import com.medicalaidtracker.appointment.AppointmentRepository;
import com.medicalaidtracker.appointment.AppointmentService;
import com.medicalaidtracker.audit.AuditService;
import com.medicalaidtracker.bed.Bed;
import com.medicalaidtracker.bed.BedDtos.BedResponse;
import com.medicalaidtracker.bed.BedRepository;
import com.medicalaidtracker.common.api.PageResponse;
import com.medicalaidtracker.common.domain.DomainEnums.*;
import com.medicalaidtracker.common.exception.ResourceNotFoundException;
import com.medicalaidtracker.config.AppProperties;
import com.medicalaidtracker.doctor.Doctor;
import com.medicalaidtracker.doctor.DoctorDtos.DoctorResponse;
import com.medicalaidtracker.doctor.DoctorRepository;
import com.medicalaidtracker.doctor.DoctorService;
import com.medicalaidtracker.hospital.Hospital;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import com.medicalaidtracker.hospital.HospitalRepository;
import com.medicalaidtracker.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.medicalaidtracker.representative.RepresentativeDtos.RepresentativeDashboardResponse;

@Service
@RequiredArgsConstructor
public class RepresentativeService {

    private final CurrentUserService currentUserService;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final AuditService auditService;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public RepresentativeDashboardResponse dashboard() {
        List<Long> hospitalIds = currentUserService.representativeHospitalIds();
        if (hospitalIds.isEmpty()) {
            return new RepresentativeDashboardResponse(0, 0, 0, 0, 0, 0, 0, List.of());
        }

        Instant dayStart = LocalDate.now(properties.timeZone())
                .atStartOfDay(properties.timeZone()).toInstant();
        Instant dayEnd = LocalDate.now(properties.timeZone()).plusDays(1)
                .atStartOfDay(properties.timeZone()).toInstant();
        List<HospitalSummary> hospitals = hospitalRepository.findAllById(hospitalIds).stream()
                .map(this::hospitalSummary)
                .toList();
        return new RepresentativeDashboardResponse(
                doctorRepository.countByHospitalIdIn(hospitalIds),
                doctorRepository.countByHospitalIdInAndAvailabilityStatus(
                        hospitalIds, AvailabilityStatus.AVAILABLE),
                bedRepository.countByHospitalIdIn(hospitalIds),
                bedRepository.countByHospitalIdInAndStatus(hospitalIds, BedStatus.AVAILABLE),
                bedRepository.countByHospitalIdInAndWardTypeAndStatus(
                        hospitalIds, WardType.ICU, BedStatus.AVAILABLE),
                bedRepository.countByHospitalIdInAndWardTypeAndStatus(
                        hospitalIds, WardType.GENERAL, BedStatus.AVAILABLE),
                appointmentRepository
                        .countByHospitalIdInAndStartsAtGreaterThanEqualAndStartsAtLessThanAndStatusNot(
                                hospitalIds, dayStart, dayEnd, AppointmentStatus.CANCELLED),
                hospitals
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> doctors(
            String query,
            Long hospitalId,
            AvailabilityStatus availabilityStatus,
            Pageable pageable
    ) {
        List<Long> hospitalIds = currentUserService.representativeHospitalIds();
        if (hospitalId != null) currentUserService.requireRepresentativeHospital(hospitalId);
        Page<Doctor> page = hospitalIds.isEmpty()
                ? Page.empty(pageable)
                : doctorRepository.findAllForRepresentative(
                        hospitalIds, hospitalId, availabilityStatus, normalize(query), pageable);
        return PageResponse.from(page, doctorService::map);
    }

    @Transactional
    public DoctorResponse updateDoctorAvailability(Long doctorId, AvailabilityStatus status) {
        Doctor doctor = doctorRepository.findDetailedById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DOCTOR_NOT_FOUND", "Doctor not found."));
        currentUserService.requireRepresentativeHospital(doctor.getHospital().getId());
        AvailabilityStatus previous = doctor.getAvailabilityStatus();
        doctor.setAvailabilityStatus(status);
        doctorRepository.save(doctor);
        var actor = currentUserService.requireUser();
        auditService.record(
                actor,
                "DOCTOR_AVAILABILITY_UPDATED",
                "Doctor",
                doctor.getId(),
                "from=" + previous + ", to=" + status
        );
        return doctorService.map(doctor);
    }

    @Transactional(readOnly = true)
    public PageResponse<BedResponse> beds(
            Long hospitalId,
            WardType wardType,
            BedStatus status,
            Pageable pageable
    ) {
        List<Long> hospitalIds = currentUserService.representativeHospitalIds();
        if (hospitalId != null) currentUserService.requireRepresentativeHospital(hospitalId);
        Page<Bed> page = hospitalIds.isEmpty()
                ? Page.empty(pageable)
                : bedRepository.findAllForRepresentative(
                        hospitalIds, hospitalId, wardType, status, pageable);
        return PageResponse.from(page, this::mapBed);
    }

    @Transactional
    public BedResponse updateBedStatus(Long bedId, BedStatus status) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BED_NOT_FOUND", "Bed not found."));
        currentUserService.requireRepresentativeHospital(bed.getHospital().getId());
        BedStatus previous = bed.getStatus();
        bed.setStatus(status);
        bedRepository.save(bed);
        var actor = currentUserService.requireUser();
        auditService.record(
                actor,
                "BED_STATUS_UPDATED",
                "Bed",
                bed.getId(),
                "from=" + previous + ", to=" + status
        );
        return mapBed(bed);
    }

    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> appointments(
            Long hospitalId,
            AppointmentStatus status,
            LocalDate dateFrom,
            LocalDate dateTo,
            OffsetDateTime startsAfter,
            Pageable pageable
    ) {
        List<Long> hospitalIds = currentUserService.representativeHospitalIds();
        if (hospitalId != null) currentUserService.requireRepresentativeHospital(hospitalId);
        Instant from = dateFrom == null ? null : dateFrom.atStartOfDay(properties.timeZone()).toInstant();
        if (startsAfter != null && (from == null || startsAfter.toInstant().isAfter(from))) {
            from = startsAfter.toInstant();
        }
        Instant to = dateTo == null
                ? null
                : dateTo.plusDays(1).atStartOfDay(properties.timeZone()).toInstant();
        Page<Appointment> page = hospitalIds.isEmpty()
                ? Page.empty(pageable)
                : appointmentRepository.findAllForRepresentative(
                        hospitalIds,
                        hospitalId,
                        status,
                        AppointmentStatus.BOOKED,
                        AppointmentStatus.COMPLETED,
                        AppointmentStatus.CANCELLED,
                        Instant.now(),
                        from,
                        to,
                        pageable);
        return PageResponse.from(page, appointmentService::map);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, AppointmentCancelRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "APPOINTMENT_NOT_FOUND", "Appointment not found."));
        currentUserService.requireRepresentativeHospital(appointment.getHospital().getId());
        return appointmentService.cancelRepresentativeAppointment(appointment, request.reason());
    }

    private BedResponse mapBed(Bed bed) {
        return new BedResponse(
                bed.getId(), bed.getBedCode(), bed.getWardType(), bed.getStatus(),
                hospitalSummary(bed.getHospital())
        );
    }

    private HospitalSummary hospitalSummary(Hospital hospital) {
        return new HospitalSummary(
                hospital.getId(), hospital.getName(), hospital.getAddress(), hospital.getContactNumber());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
