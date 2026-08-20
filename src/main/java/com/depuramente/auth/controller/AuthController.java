package com.depuramente.auth.controller;

import com.depuramente.auth.dto.*;
import com.depuramente.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** HTTP API for registration, authentication, token lifecycle, and validation. */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     * @param request registration data
     * @return HTTP 201 with the public registration result
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates user credentials.
     * @param request login credentials
     * @return HTTP 200 with access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Rotates a refresh token and issues a new access token.
     * @param request refresh token request
     * @return HTTP 200 with the new token pair
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    /**
     * Validates the bearer access token.
     * @param accessToken Authorization header value
     * @return HTTP 200 with token claims
     */
    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestHeader(value = "Authorization") String accessToken) {
        return ResponseEntity.ok(authService.validate(accessToken));
    }

    /**
     * Revokes one refresh token.
     * @param request logout request containing the refresh token
     * @return HTTP 200 with an empty body
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Revokes all refresh tokens for the authenticated user.
     * @param accessToken Authorization header value
     * @return HTTP 200 with an empty body
     */
    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutAll(@RequestHeader(value = "Authorization") String accessToken) {
        authService.logoutAll(accessToken);
        return ResponseEntity.ok().build();
    }
}
