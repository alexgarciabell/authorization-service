package com.depuramente.auth.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Password1",
            "P@ssword1!",
            "12345678",
            "Abcdefghijklmnopqrs1"
    })
    void acceptsPasswordsWithinPolicy(String password) {
        assertTrue(PasswordValidator.isValid(password));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "short7",
            "Abcdefghijklmnopqrs12",
            "Password 1",
            "密码密码密码",
            "Password%1"
    })
    void rejectsPasswordsOutsidePolicy(String password) {
        assertFalse(PasswordValidator.isValid(password));
    }
}
