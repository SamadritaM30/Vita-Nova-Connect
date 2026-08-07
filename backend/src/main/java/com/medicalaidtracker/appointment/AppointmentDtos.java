package com.medicalaidtracker.appointment;

import com.medicalaidtracker.common.domain.DomainEnums.AppointmentStatus;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AppointmentDtos {

    private AppointmentDtos() {
    }

    public record DoctorSummary(
            Long id,
            String name,
            String specialization
    ) {
    }

    public record PatientSummary(
            UUID id,
            String name,
            String contactNumber
    ) {
    }

    public record AppointmentResponse(
            UUID id,
            AppointmentStatus status,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            String cancellationReason,
            DoctorSummary doctor,
            HospitalSummary hospital,
            PatientSummary patient
    ) {
    }

    public record AppointmentCreateRequest(
            @NotNull(message = "Doctor ID is required.")
            Long doctorId,

            @NotNull(message = "Appointment start time is required.")
            @Future(message = "Appointment start time must be in the future.")
            OffsetDateTime startsAt
    ) {
    }

    public record AppointmentCancelRequest(
            @NotBlank(message = "Cancellation reason is required.")
            @Size(min = 3, max = 500, message = "Cancellation reason must contain 3 to 500 characters.")
            String reason
    ) {
    }
}
