package com.depuramente.auth.dto;

public record LogoutRequest(
        String refreshToken
) {
}
