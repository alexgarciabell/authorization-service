package com.depuramente.auth.util;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern PATTERN =
            Pattern.compile("^[a-zA-Z0-9.*$!#@\\-_?]{8,20}$");

    private PasswordValidator() {
        // Utility class.
    }

    /**
     * Checks the password policy used during registration.
     *
     * <p>A valid password is 8 to 20 characters long and may contain letters,
     * digits, and the symbols {@code . * $ ! # @ - _ ?}.</p>
     *
     * @param password value to validate; {@code null} is invalid
     * @return {@code true} when the value satisfies the password policy
     */
    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }
}
