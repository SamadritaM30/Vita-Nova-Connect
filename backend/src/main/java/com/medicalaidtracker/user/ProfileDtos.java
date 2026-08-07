package com.medicalaidtracker.user;

import com.medicalaidtracker.common.domain.DomainEnums.Role;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record HospitalAssignment(
            Long id,
            String name
    ) {
    }

    public record ProfileResponse(
            UUID userId,
            String email,
            String displayName,
            String avatarUrl,
            Role role,
            LocalDate dateOfBirth,
            String contactNumber,
            String bloodGroup,
            String address,
            String employeeId,
            String organizationName,
            List<HospitalAssignment> assignedHospitals,
            boolean completed
    ) {
    }

    public record ProfileUpdateRequest(
            @Size(min = 2, max = 120, message = "Display name must contain 2 to 120 characters.")
            @Pattern(
                    regexp = "^(?s).*\\S.*\\S.*$",
                    message = "Display name must contain at least 2 non-whitespace characters."
            )
            String displayName,

            @Past(message = "Date of birth must be in the past.")
            LocalDate dateOfBirth,

            @Pattern(
                    regexp = "^[+]?[0-9][0-9 -]{7,18}$",
                    message = "Enter a valid contact number."
            )
            String contactNumber,

            @Pattern(
                    regexp = "^(A|B|AB|O)[+-]$",
                    message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, or O-."
            )
            String bloodGroup,

            @Size(max = 500, message = "Address cannot exceed 500 characters.")
            String address,

            @Size(max = 80, message = "Employee ID cannot exceed 80 characters.")
            String employeeId,

            @Size(max = 160, message = "Organization name cannot exceed 160 characters.")
            String organizationName
    ) {
    }
}
