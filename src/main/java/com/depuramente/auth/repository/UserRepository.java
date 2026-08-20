package com.depuramente.auth.repository;

import com.depuramente.auth.model.DPMUser;

import java.util.Optional;

/** Persistence contract for registered users. */
public interface UserRepository {

    /**
     * Finds a user by username.
     * @param email username, represented by an email address
     * @return matching user, or empty when none exists
     */
    Optional<DPMUser> findById(String email);

    /**
     * Stores or replaces a user.
     * @param user user entity to persist
     */
    void save(DPMUser user);

    /**
     * Deletes a user by username.
     * @param email username, represented by an email address
     */
    void delete(String email);
}
