package com.depuramente.auth.controller;

import com.depuramente.auth.dto.*;
import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.service.AuthService;
import com.depuramente.auth.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshService;

    public AuthController(AuthService authService, RefreshTokenService refreshService) {
        this.authService = authService;
        this.refreshService = refreshService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshToken> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshService.create(request.username()));
    }
}
