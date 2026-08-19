package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.model.DPMRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtService {
    private final static Logger LOG = LoggerFactory.getLogger(JwtService.class);
    private static final String ROLES = "roles";
    private final JWTProperties jwtProperties;
    private final SecretKey key;

    public JwtService(JWTProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is missing");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String username, Set<DPMRole> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getTokenExpiration().toSeconds());

        return Jwts.builder()
                .subject(username)
                .claim(ROLES, roles.stream().map(Enum::name).collect(Collectors.toSet()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Set<DPMRole> extractRoles(String token) {
        Claims claims = getClaims(token);
        List<String> roleNames = claims.get(ROLES, List.class);
        Set<String> roles = new HashSet<>(roleNames);


        return roles.stream()
                .map(DPMRole::valueOf)
                .collect(Collectors.toSet());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }
}
