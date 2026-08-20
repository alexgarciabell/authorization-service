package com.depuramente.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/** Configuration values controlling JWT signing and token lifetimes. */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JWTProperties {
    private String secret;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration accessTokenExpiration;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration refreshTokenExpiration;

    /** @return configured JWT signing secret */
    public String getSecret() {
        return secret;
    }

    /** @param secret JWT signing secret */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /** @return access-token lifetime */
    public Duration getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /** @param accessTokenExpiration access-token lifetime */
    public void setAccessTokenExpiration(Duration accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    /** @return refresh-token lifetime */
    public Duration getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    /** @param refreshTokenExpiration refresh-token lifetime */
    public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
