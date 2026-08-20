package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

public record ValidateResponse(
        boolean isValid,
        String username,
        Set<DPMRole> roles
) {
}
