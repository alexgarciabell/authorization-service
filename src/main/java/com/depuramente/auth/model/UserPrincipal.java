package com.depuramente.auth.model;


import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/** Adapts a persisted user to Spring Security's {@link UserDetails} contract. */
public class UserPrincipal implements UserDetails {
    private final DPMUser user;

    /**
     * Creates a security principal for a persisted user.
     * @param user persisted user to adapt
     */
    public UserPrincipal(DPMUser user) {
        this.user = user;
    }


    /**
     * Returns authorities derived from the user's roles.
     * @return granted authorities for the user
     */
    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> (GrantedAuthority) role::name)
                .toList();
    }

    /** @return stored password hash */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** @return user's login identifier */
    @Override
    @NonNull
    public String getUsername() {
        return user.getUsername();
    }

    /** @return {@code true}; account expiry is not currently modeled */
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    /** @return {@code true}; account lock state is not currently modeled */
    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    /** @return {@code true}; credential expiry is not currently modeled */
    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    /** @return {@code true} when the principal is enabled */
    @Override
    public boolean isEnabled() {
        return false;
    }
}
