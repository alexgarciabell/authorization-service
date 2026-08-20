package com.depuramente.auth.dto;

/**
 * Credentials submitted when a user attempts to log in.
 * @param username user's login identifier
 * @param password user's raw password
 */
public record AuthRequest(
        String username,
        String password
) {
}
