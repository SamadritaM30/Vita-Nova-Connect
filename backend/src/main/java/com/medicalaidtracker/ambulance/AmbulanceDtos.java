package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceRequestStatus;
import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceStatus;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AmbulanceDtos {

    private AmbulanceDtos() {
    }

    public record AmbulanceResponse(
            Long id,
            String vehicleNumber,
            String driverName,
            String contactNumber,
            String lastLocation,
            AmbulanceStatus status,
            HospitalSummary hospital
    ) {
    }

    public record AmbulanceRequestCreate(
            @NotNull(message = "Hospital ID is required.")
            Long hospitalId,

            @NotBlank(message = "Pickup address is required.")
            @Size(max = 500, message = "Pickup address cannot exceed 500 characters.")
            String pickupAddress,

            @NotBlank(message = "Contact number is required.")
            @Pattern(
                    regexp = "^[+]?[0-9][0-9 -]{7,18}$",
                    message = "Enter a valid contact number."
            )
            String contactNumber
    ) {
    }

    public record AmbulanceRequestResponse(
            UUID id,
            AmbulanceRequestStatus status,
            String pickupAddress,
            String contactNumber,
            OffsetDateTime requestedAt,
            HospitalSummary hospital,
            AmbulanceResponse ambulance
    ) {
    }
}
