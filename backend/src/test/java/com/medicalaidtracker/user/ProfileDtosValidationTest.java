package com.medicalaidtracker.user;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileDtosValidationTest {

    @Test
    void rejectsDisplayNameThatBecomesEmptyAfterTrimming() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new ProfileDtos.ProfileUpdateRequest(
                    "   ", null, null, null, null, null, null);

            assertThat(factory.getValidator().validate(request))
                    .anyMatch(violation ->
                            violation.getPropertyPath().toString().equals("displayName"));
        }
    }
}
