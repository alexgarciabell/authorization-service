package com.depuramente.auth.dto;

public record AuthRequest(
        String id,
        String password
) {
}
