package com.depuramente.auth;

import com.depuramente.auth.config.JWTProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

@SpringBootTest
@ActiveProfiles("local")
class AuthorizationServiceApplicationTests {

    @Autowired
    private JWTProperties jwtProperties;

    @Test
    void contextLoads() {
    }

    @Test
    void jwtExpirationPropertiesAreBound() {
        org.junit.jupiter.api.Assertions.assertEquals(Duration.ofMinutes(5), jwtProperties.getTokenExpiration());
        org.junit.jupiter.api.Assertions.assertEquals(Duration.ofDays(1), jwtProperties.getRefreshTokenExpiration());
    }

}
