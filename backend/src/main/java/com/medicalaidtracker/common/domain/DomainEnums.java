package com.medicalaidtracker.common.domain;

public final class DomainEnums {

    private DomainEnums() {
    }

    public enum Role {
        PATIENT,
        REPRESENTATIVE
    }

    public enum UserStatus {
        ACTIVE,
        SUSPENDED
    }

    public enum AuthProvider {
        GOOGLE
    }

    public enum AvailabilityStatus {
        AVAILABLE,
        UNAVAILABLE
    }

    public enum WardType {
        ICU,
        GENERAL
    }

    public enum BedStatus {
        AVAILABLE,
        OCCUPIED,
        MAINTENANCE
    }

    public enum AppointmentStatus {
        BOOKED,
        COMPLETED,
        CANCELLED
    }

    public enum AmbulanceStatus {
        AVAILABLE,
        UNAVAILABLE,
        ON_DUTY
    }

    public enum AmbulanceRequestStatus {
        PENDING,
        ASSIGNED,
        COMPLETED,
        CANCELLED
    }
}
