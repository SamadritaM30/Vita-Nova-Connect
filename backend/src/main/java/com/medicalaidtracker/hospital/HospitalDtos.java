package com.medicalaidtracker.hospital;

public final class HospitalDtos {

    private HospitalDtos() {
    }

    public record HospitalSummary(
            Long id,
            String name,
            String address,
            String contactNumber
    ) {
    }

    public record HospitalResponse(
            Long id,
            String name,
            String address,
            String contactNumber,
            long availableIcuBeds,
            long availableGeneralBeds,
            long availableDoctors
    ) {
    }
}
