package com.depuramente.auth.service;

import com.depuramente.auth.model.DPMUser;
import com.depuramente.auth.repository.UserRepository;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads persisted users for Spring Security authentication. */
@Service
public class UserPrincipalImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserPrincipalImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user and adapts it to Spring Security.
     *
     * @param id username used to locate the user
     * @return Spring Security user details
     * @throws UsernameNotFoundException when no user matches the ID
     */
    @Override
    @Nonnull
    public UserDetails loadUserByUsername(@Nonnull String id) throws UsernameNotFoundException {
        DPMUser user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("user %s not found", id)));
        return new com.depuramente.auth.model.UserPrincipal(user);
    }
}
