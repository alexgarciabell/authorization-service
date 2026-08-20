package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

/**
 * User details submitted during account registration.
 * @param username user's email/login identifier
 * @param alias display name
 * @param password raw password
 * @param roles roles assigned to the user
 */
public record RegisterRequest(
        String username,
        String alias,
        String password,
        Set<DPMRole> roles
) {
}
