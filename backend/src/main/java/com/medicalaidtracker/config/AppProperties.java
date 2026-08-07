package com.medicalaidtracker.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank
        @Pattern(regexp = "^https?://.+", message = "must be an absolute HTTP(S) URL")
        String frontendUrl,

        @NotEmpty
        List<@NotBlank String> corsAllowedOrigins,

        @NotNull
        ZoneId timeZone,

        @Min(value = 5, message = "must be at least 5 minutes")
        @Max(value = 240, message = "must be no more than 240 minutes")
        int appointmentDurationMinutes,

        @Valid
        @NotNull
        OAuth oauth
) {
    public record OAuth(
            @NotBlank String successPath,
            @NotBlank String failurePath
    ) {
    }
}
