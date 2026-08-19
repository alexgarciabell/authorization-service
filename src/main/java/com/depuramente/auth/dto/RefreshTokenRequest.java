package com.depuramente.auth.dto;

public record RefreshTokenRequest(
        String username,
        String refreshToken
) {
}
