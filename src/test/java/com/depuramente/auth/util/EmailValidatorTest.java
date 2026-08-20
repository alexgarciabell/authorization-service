package com.depuramente.auth.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last+tag@example-domain.com",
            "user_123@sub.example.org"
    })
    void acceptsSupportedEmailFormats(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "user",
            "@example.com",
            "user@",
            "user name@example.com",
            "user@example .com"
    })
    void rejectsInvalidEmailFormats(String email) {
        assertFalse(EmailValidator.isValid(email));
    }
}
