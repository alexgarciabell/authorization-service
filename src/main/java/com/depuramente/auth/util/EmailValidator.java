package com.depuramente.auth.util;

import java.util.regex.Pattern;

public final class EmailValidator {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isValid(String email) {
        return email != null && PATTERN.matcher(email).matches();
    }
}
