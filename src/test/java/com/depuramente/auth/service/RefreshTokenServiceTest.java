package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    @Mock
    private JWTProperties jwtProperties;

    @Test
    void createGeneratesAndPersistsActiveTokenWithConfiguredExpiry() {
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);
        Instant before = Instant.now();

        RefreshToken result = service.create("user@example.com");

        Instant after = Instant.now();
        assertEquals("user@example.com", result.getUsername());
        assertTrue(result.getId() != null && !result.getId().isBlank());
        assertTrue(result.getToken() != null && !result.getToken().isBlank());
        assertFalse(result.isRevoked());
        assertTrue(!result.getExpiresAt().isBefore(before.plus(Duration.ofDays(7)).minusSeconds(1)));
        assertTrue(!result.getExpiresAt().isAfter(after.plus(Duration.ofDays(7)).plusSeconds(1)));
        verify(repository).save(result);
    }

    @Test
    void validateReturnsActiveUnexpiredToken() {
        RefreshToken token = token(false, Instant.now().plusSeconds(60));
        when(repository.findByToken("value")).thenReturn(Optional.of(token));
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        assertEquals(token, service.validate("value"));
    }

    @Test
    void validateRejectsUnknownToken() {
        when(repository.findByToken("missing")).thenReturn(Optional.empty());
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> service.validate("missing"));

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void validateRejectsRevokedToken() {
        when(repository.findByToken("revoked")).thenReturn(
                Optional.of(token(true, Instant.now().plusSeconds(60))));
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> service.validate("revoked"));

        assertEquals("Refresh token revoked", exception.getMessage());
    }

    @Test
    void validateRejectsExpiredToken() {
        when(repository.findByToken("expired")).thenReturn(
                Optional.of(token(false, Instant.now().minusSeconds(1))));
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> service.validate("expired"));

        assertEquals("Refresh token expired", exception.getMessage());
    }

    @Test
    void revokeMarksTokenRevokedAndPersistsIt() {
        RefreshToken token = token(false, Instant.now().plusSeconds(60));
        when(repository.findByToken("value")).thenReturn(Optional.of(token));
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        service.revoke("value");

        assertTrue(token.isRevoked());
        verify(repository).save(token);
    }

    @Test
    void revokeRejectsUnknownTokenWithoutSaving() {
        when(repository.findByToken("missing")).thenReturn(Optional.empty());
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        assertThrows(IllegalArgumentException.class, () -> service.revoke("missing"));

        verify(repository, never()).save(any());
    }

    @Test
    void revokeAllForUserRevokesAndPersistsEveryToken() {
        RefreshToken first = token(false, Instant.now().plusSeconds(60));
        RefreshToken second = token(false, Instant.now().plusSeconds(60));
        when(repository.findAllByUsername("user@example.com"))
                .thenReturn(List.of(first, second));
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        service.revokeAllForUser("user@example.com");

        assertTrue(first.isRevoked());
        assertTrue(second.isRevoked());
        verify(repository).save(first);
        verify(repository).save(second);
    }

    @Test
    void generateOpaqueTokenReturnsUniqueUrlSafeValues() {
        RefreshTokenService service = new RefreshTokenService(repository, jwtProperties);

        String first = service.generateOpaqueToken();
        String second = service.generateOpaqueToken();

        assertEquals(86, first.length());
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
        assertNotEquals(first, second);
        verifyNoInteractions(repository, jwtProperties);
    }

    private static RefreshToken token(boolean revoked, Instant expiresAt) {
        return new RefreshToken("id", "user@example.com", "value", expiresAt, revoked);
    }
}
