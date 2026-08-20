package com.depuramente.auth.repository;

import com.depuramente.auth.model.RefreshToken;

import java.util.List;
import java.util.Optional;

/** Persistence contract for opaque refresh tokens. */
public interface RefreshTokenRepository {

    /**
     * Stores or replaces a refresh token.
     * @param token token entity to persist
     */
    void save(RefreshToken token);

    /**
     * Finds a token by its user partition key and token ID.
     * @param username token owner
     * @param id token sort-key identifier
     * @return matching token, or empty when none exists
     */
    Optional<RefreshToken> findById(String username, String id);

    /**
     * Finds a token by its opaque value.
     * @param token opaque refresh-token value
     * @return matching token, or empty when none exists
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Lists all tokens owned by a user.
     * @param username token owner
     * @return tokens belonging to the user
     */
    List<RefreshToken> findAllByUsername(String username);

    /**
     * Deletes a token by its composite key.
     * @param username token owner
     * @param id token sort-key identifier
     */
    void delete(String username, String id);

}
