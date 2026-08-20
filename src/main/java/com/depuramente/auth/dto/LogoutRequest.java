package com.depuramente.auth.dto;

/**
 * Request containing the refresh token to revoke during logout.
 * @param refreshToken opaque refresh token to revoke
 */
public record LogoutRequest(
        String refreshToken
) {
}
