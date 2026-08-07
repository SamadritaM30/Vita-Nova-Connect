package com.medicalaidtracker.user;

import com.medicalaidtracker.common.api.ApiResponse;
import com.medicalaidtracker.security.CurrentUserService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.medicalaidtracker.user.AuthDtos.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CurrentUserService currentUserService;
    private final ProfileService profileService;

    public AuthController(CurrentUserService currentUserService, ProfileService profileService) {
        this.currentUserService = currentUserService;
        this.profileService = profileService;
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me() {
        var user = currentUserService.requireUser();
        return ApiResponse.ok(new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getStatus(),
                profileService.isProfileComplete(user)
        ));
    }

    @GetMapping("/csrf")
    ApiResponse<CsrfResponse> csrf(CsrfToken csrfToken) {
        return ApiResponse.ok(new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        ));
    }
}
