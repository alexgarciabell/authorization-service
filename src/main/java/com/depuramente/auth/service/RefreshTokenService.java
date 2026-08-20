package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/** Manages opaque refresh-token creation, validation, rotation, and revocation. */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final JWTProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshRepository, JWTProperties jwtProperties) {
        this.refreshRepository = refreshRepository;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Creates and persists a random active refresh token.
     * @param username owner of the token
     * @return newly persisted refresh token
     */
    public RefreshToken create(String username) {
        var expiresAt = Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpiration().toSeconds());
        var token = new RefreshToken(
                UUID.randomUUID().toString(),
                username,
                generateOpaqueToken(),
                expiresAt,
                false
        );
        refreshRepository.save(token);
        return token;
    }

    /**
     * Checks that a token exists, is active, and has not expired.
     * @param token opaque token value supplied by a client
     * @return validated persisted token
     * @throws IllegalArgumentException when the token does not exist
     * @throws RuntimeException when the token is revoked or expired
     */
    public RefreshToken validate(String token) {
        var stored = refreshRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return stored;
    }

    /**
     * Revokes and persists one refresh token.
     * @param tokenValue opaque token value to revoke
     * @throws IllegalArgumentException when the token does not exist
     */
    public void revoke(String tokenValue) {
        RefreshToken stored = refreshRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        stored.setRevoked(true);
        refreshRepository.save(stored);

    }

    /**
     * Revokes every refresh token owned by a user.
     * @param username owner whose tokens should be revoked
     */
    public void revokeAllForUser(String username) {
        var tokens = refreshRepository.findAllByUsername(username);
        tokens.forEach(t -> {
            t.setRevoked(true);
            refreshRepository.save(t);
        });
    }

    /**
     * Generates a cryptographically random URL-safe opaque token.
     * @return random token value without padding characters
     */
    public String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
