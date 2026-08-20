package com.depuramente.auth.dto;

import com.depuramente.auth.model.DPMRole;

import java.util.Set;

/**
 * Public registration result containing the alias and assigned roles.
 * @param username registered user's public alias
 * @param roles roles assigned to the user
 */
public record RegisterResponse(
        String username,
        Set<DPMRole> roles
) {
}
