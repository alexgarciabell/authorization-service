package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.dto.AuthRequest;
import com.depuramente.auth.dto.RegisterRequest;
import com.depuramente.auth.dto.RegisterResponse;
import com.depuramente.auth.dto.TokenResponse;
import com.depuramente.auth.model.DPMUser;
import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.repository.UserRepository;
import com.depuramente.auth.util.EmailValidator;
import com.depuramente.auth.util.PasswordValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Application service for user registration and credential-based authentication.
 *
 * <p>The service validates registration input, hashes passwords with BCrypt,
 * persists users, and creates JWT access and refresh tokens during login.
 * Refresh-token rotation is handled by {@link RefreshTokenService}.</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;
    private final JWTProperties jwtProperties;

    public AuthService(UserRepository userRepo, RefreshTokenService refreshTokenService, BCryptPasswordEncoder encoder, JwtService jwtService, JWTProperties jwtProperties) {
        this.userRepo = userRepo;
        this.refreshTokenService = refreshTokenService;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }


    /**
     * Registers a user after validating the email and password.
     *
     * <p>The request ID is used as both the user's persistent ID and username.
     * The raw password is never persisted; it is encoded with BCrypt first.
     * The supplied roles and timestamps are stored with the new user.</p>
     *
     * @param request registration data containing the email, password, and roles
     * @throws IllegalArgumentException if the email or password is invalid
     */
    public RegisterResponse register(RegisterRequest request) {

        if (!EmailValidator.isValid(request.username()))
            throw new IllegalArgumentException("Invalid email");

        if (!PasswordValidator.isValid(request.password()))
            throw new IllegalArgumentException("Invalid password");

        DPMUser user = new DPMUser();
        user.setUsername(request.username());
        user.setAlias(request.alias());
        user.setPassword(encoder.encode(request.password()));
        user.setRoles(request.roles());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userRepo.save(user);
        return new RegisterResponse(user.getAlias(), user.getRoles());
    }

    /**
     * Authenticates a user and issues an access token and refresh token.
     *
     * <p>The refresh token is persisted as active before the response is
     * returned. The response's {@code expiresIn} value is expressed in
     * seconds and is currently set to 900.</p>
     *
     * @param request credentials containing the user ID and raw password
     * @return the newly issued access and refresh tokens
     * @throws IllegalArgumentException if the user does not exist or the
     *                                  password does not match
     */
    public TokenResponse login(AuthRequest request) {

        DPMUser user = userRepo.findById(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!encoder.matches(request.password(), user.getPassword()))
            throw new IllegalArgumentException("Invalid credentials");

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRoles());
        RefreshToken refreshToken = refreshTokenService.create(user.getUsername());

        return new TokenResponse(accessToken, refreshToken.getToken(), jwtProperties.getTokenExpiration().getSeconds(), "Bearer", user.getUsername(), user.getRoles());

    }

    /**
     * Validates a refresh token and issues a new access/refresh-token pair.
     * The username is taken from the persisted refresh token rather than from
     * the request, so a caller cannot refresh a token for another user.
     */
    public TokenResponse refresh(String refreshToken) {
        RefreshToken currentToken = refreshTokenService.validate(refreshToken);
        DPMUser user = userRepo.findById(currentToken.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRoles());

        refreshTokenService.revoke(refreshToken);
        RefreshToken nextRefreshToken = refreshTokenService.create(user.getUsername());

        return new TokenResponse(
                accessToken,
                nextRefreshToken.getToken(),
                jwtProperties.getTokenExpiration().getSeconds(),
                "Bearer",
                user.getUsername(),
                user.getRoles()
        );
    }

}
