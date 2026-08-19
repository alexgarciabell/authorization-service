package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        String username,
        Set<DPMRole> dpmRoles
) {
}
