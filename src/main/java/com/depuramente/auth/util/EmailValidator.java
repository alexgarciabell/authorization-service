package com.depuramente.auth.util;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private EmailValidator() {
        // Utility class.
    }

    /**
     * Checks whether a value has the basic email shape required by user
     * registration. This is intentionally a lightweight format check; it does
     * not verify that the address exists or that its domain can receive mail.
     *
     * @param email value to validate; {@code null} is invalid
     * @return {@code true} when the value matches the supported email format
     */
    public static boolean isValid(String email) {
        return email != null && PATTERN.matcher(email).matches();
    }
}
