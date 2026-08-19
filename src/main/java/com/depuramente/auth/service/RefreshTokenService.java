package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.repository.RefreshTokenRepository;
import com.depuramente.auth.repository.UserRepository;
import com.depuramente.auth.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final JWTProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshRepository, JWTProperties jwtProperties) {
        this.refreshRepository = refreshRepository;
        this.jwtProperties = jwtProperties;
    }

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

    public void revoke(String tokenValue) {
        RefreshToken stored = refreshRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        stored.setRevoked(true);
        refreshRepository.save(stored);

    }

    public void revokeAllForUser(String username) {
        var tokens = refreshRepository.findAllByUsername(username);
        tokens.forEach(t -> {
            t.setRevoked(true);
            refreshRepository.save(t);
        });
    }

    public String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
