package com.depuramente.auth.dto;

/**
 * Request carrying the refresh token used to obtain a new token pair.
 * @param username legacy client-supplied username; server identity comes from the token
 * @param refreshToken opaque refresh token to rotate
 */
public record RefreshTokenRequest(
        String username,
        String refreshToken
) {
}
