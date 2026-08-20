package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

/**
 * Complete token response shared by login and refresh endpoints.
 * @param accessToken signed access JWT
 * @param refreshToken rotated opaque refresh token
 * @param expiresIn access-token lifetime in seconds
 * @param tokenType authorization scheme, normally {@code Bearer}
 * @param username authenticated username
 * @param dpmRoles roles encoded in the access token
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        String username,
        Set<DPMRole> dpmRoles
) {
}
