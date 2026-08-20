package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.model.DPMRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void constructorRejectsMissingSecret() {
        JWTProperties properties = new JWTProperties();

        assertThrows(IllegalStateException.class, () -> new JwtService(properties));

        properties.setSecret("   ");
        assertThrows(IllegalStateException.class, () -> new JwtService(properties));
    }

    @Test
    void generateAccessTokenContainsSubjectRolesAndConfiguredExpiration() {
        Duration expiration = Duration.ofMinutes(5);
        JwtService service = service(expiration);
        Set<DPMRole> roles = Set.of(DPMRole.ROLE_USER, DPMRole.ROLE_STAFF);

        Instant beforeGeneration = Instant.now();
        String token = service.generateAccessToken("user@example.com", roles);
        Instant afterGeneration = Instant.now();

        Claims claims = parse(token, secretKey(SECRET));
        assertEquals("user@example.com", claims.getSubject());
        assertEquals(roles, service.extractRoles(token));
        assertTrue(service.validateToken(token));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(!claims.getIssuedAt().toInstant().isBefore(beforeGeneration.minusSeconds(1)));
        assertTrue(!claims.getIssuedAt().toInstant().isAfter(afterGeneration));
        assertTrue(!claims.getExpiration().toInstant().isBefore(beforeGeneration.plus(expiration).minusSeconds(1)));
    }

    @Test
    void extractUsernameReturnsSubjectFromValidToken() {
        JwtService service = service(Duration.ofMinutes(5));
        String token = service.generateAccessToken("user@example.com", Set.of(DPMRole.ROLE_ADMIN));

        assertEquals("user@example.com", service.extractUsername(token));
    }

    @Test
    void validateTokenRejectsMalformedToken() {
        JwtService service = service(Duration.ofMinutes(5));

        assertFalse(service.validateToken("not-a-jwt"));
        assertFalse(service.validateToken(null));
        assertFalse(service.validateToken(""));
    }

    @Test
    void validateTokenRejectsTokenSignedWithAnotherSecret() {
        JwtService service = service(Duration.ofMinutes(5));
        SecretKey otherKey = secretKey("abcdefghijklmnopqrstuvwxyz123456");
        String token = Jwts.builder()
                .subject("user@example.com")
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        assertFalse(service.validateToken(token));
    }

    @Test
    void validateTokenRejectsExpiredToken() {
        JwtService service = service(Duration.ofMinutes(5));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(secretKey(SECRET), Jwts.SIG.HS256)
                .compact();

        assertFalse(service.validateToken(token));
    }

    @Test
    void extractClaimsRejectsInvalidToken() {
        JwtService service = service(Duration.ofMinutes(5));

        assertThrows(RuntimeException.class, () -> service.extractUsername("not-a-jwt"));
        assertThrows(RuntimeException.class, () -> service.extractRoles("not-a-jwt"));
    }

    private static JwtService service(Duration expiration) {
        JWTProperties properties = new JWTProperties();
        properties.setSecret(SECRET);
        properties.setTokenExpiration(expiration);
        return new JwtService(properties);
    }

    private static SecretKey secretKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static Claims parse(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
