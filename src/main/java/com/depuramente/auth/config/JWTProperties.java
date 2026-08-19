package com.depuramente.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JWTProperties {
    private String secret;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration tokenExpiration;
    @DurationUnit(ChronoUnit.DAYS)
    private Duration refreshTokenExpiration;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(Duration tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
