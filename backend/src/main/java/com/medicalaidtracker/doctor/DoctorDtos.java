package com.medicalaidtracker.doctor;

import com.medicalaidtracker.common.domain.DomainEnums.AvailabilityStatus;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;

import java.time.LocalTime;
import java.time.OffsetDateTime;

public final class DoctorDtos {

    private DoctorDtos() {
    }

    public record DoctorResponse(
            Long id,
            String name,
            String specialization,
            AvailabilityStatus availabilityStatus,
            LocalTime consultationFrom,
            LocalTime consultationTo,
            HospitalSummary hospital
    ) {
    }

    public record AppointmentSlotResponse(
            OffsetDateTime startsAt,
            OffsetDateTime endsAt
    ) {
    }

    public record DoctorAvailabilityUpdateRequest(
            @jakarta.validation.constraints.NotNull(message = "Availability status is required.")
            AvailabilityStatus availabilityStatus
    ) {
    }
}
