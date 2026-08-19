package com.depuramente.auth.util;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern PATTERN =
            Pattern.compile("^[a-zA-Z0-9.*$!#@\\-_?]{8,20}$");

    public static boolean isValid(String password) {
        return password != null && PATTERN.matcher(password).matches();
    }
}
