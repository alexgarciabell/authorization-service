package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

/**
 * Result of access-token validation, including its subject and roles.
 * @param isValid whether the access token is valid
 * @param username token subject
 * @param roles roles extracted from the token
 */
public record ValidateResponse(
        boolean isValid,
        String username,
        Set<DPMRole> roles
) {
}
