package com.depuramente.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {

}
