package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

public record RegisterResponse(
        String username,
        Set<DPMRole> roles
) {
}
