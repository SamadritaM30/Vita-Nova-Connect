package com.medicalaidtracker.user;

import com.medicalaidtracker.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.medicalaidtracker.user.ProfileDtos.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    ApiResponse<ProfileResponse> getProfile() {
        return ApiResponse.ok(profileService.getCurrentProfile());
    }

    @PatchMapping
    ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(profileService.updateCurrentProfile(request));
    }
}
