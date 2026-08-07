package com.medicalaidtracker.config;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class AppPropertiesValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void rejectsUnsafeAppointmentDurationConfiguration() {
        var properties = new AppProperties(
                "https://app.example.com",
                List.of("https://app.example.com"),
                ZoneId.of("Asia/Kolkata"),
                4,
                new AppProperties.OAuth("/auth/callback", "/auth/error")
        );

        assertThat(validator.validate(properties))
                .anyMatch(violation ->
                        violation.getPropertyPath().toString()
                                .equals("appointmentDurationMinutes"));
    }
}
