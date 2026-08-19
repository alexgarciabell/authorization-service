package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

public record RegisterRequest(
        String username,
        String alias,
        String password,
        Set<DPMRole> roles
) {
}
