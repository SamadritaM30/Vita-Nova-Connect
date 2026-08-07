package com.medicalaidtracker.user;

import com.medicalaidtracker.common.domain.DomainEnums.Role;
import com.medicalaidtracker.common.domain.DomainEnums.UserStatus;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record UserResponse(
            UUID id,
            String email,
            String displayName,
            String avatarUrl,
            Role role,
            UserStatus status,
            boolean profileCompleted
    ) {
    }

    public record CsrfResponse(
            String headerName,
            String parameterName,
            String token
    ) {
    }
}
