package com.medicalaidtracker.bed;

import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;
import jakarta.validation.constraints.NotNull;

public final class BedDtos {

    private BedDtos() {
    }

    public record BedResponse(
            Long id,
            String bedCode,
            WardType wardType,
            BedStatus status,
            HospitalSummary hospital
    ) {
    }

    public record BedStatusUpdateRequest(
            @NotNull(message = "Bed status is required.")
            BedStatus status
    ) {
    }
}
