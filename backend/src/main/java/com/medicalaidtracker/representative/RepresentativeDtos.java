package com.medicalaidtracker.representative;

import com.medicalaidtracker.hospital.HospitalDtos.HospitalSummary;

import java.util.List;

public final class RepresentativeDtos {

    private RepresentativeDtos() {
    }

    public record RepresentativeDashboardResponse(
            long totalDoctors,
            long availableDoctors,
            long totalBeds,
            long availableBeds,
            long availableIcuBeds,
            long availableGeneralBeds,
            long todayAppointments,
            List<HospitalSummary> assignedHospitals
    ) {
    }
}
