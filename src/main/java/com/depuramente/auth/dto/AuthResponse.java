package com.depuramente.auth.dto;

/**
 * Access and refresh tokens returned by authentication operations.
 * @param accessToken signed access JWT
 * @param refreshToken opaque refresh token
 * @param expiresIn access-token lifetime in seconds
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {

}
