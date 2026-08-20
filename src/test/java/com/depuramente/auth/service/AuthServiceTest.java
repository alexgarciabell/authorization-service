package com.depuramente.auth.service;

import com.depuramente.auth.config.JWTProperties;
import com.depuramente.auth.dto.AuthRequest;
import com.depuramente.auth.dto.RegisterRequest;
import com.depuramente.auth.dto.RegisterResponse;
import com.depuramente.auth.dto.TokenResponse;
import com.depuramente.auth.dto.ValidateResponse;
import com.depuramente.auth.model.DPMRole;
import com.depuramente.auth.model.DPMUser;
import com.depuramente.auth.model.RefreshToken;
import com.depuramente.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JWTProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerValidUserHashesPasswordPersistsUserAndReturnsSummary() {
        Set<DPMRole> roles = Set.of(DPMRole.ROLE_USER);
        RegisterRequest request = new RegisterRequest(
                "user@example.com", "User", "Password1!", roles);
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed-password");

        RegisterResponse response = authService.register(request);

        assertEquals("User", response.username());
        assertEquals(roles, response.roles());

        ArgumentCaptor<DPMUser> userCaptor = ArgumentCaptor.forClass(DPMUser.class);
        verify(userRepository).save(userCaptor.capture());
        DPMUser saved = userCaptor.getValue();
        assertEquals("user@example.com", saved.getUsername());
        assertEquals("User", saved.getAlias());
        assertEquals("hashed-password", saved.getPassword());
        assertEquals(roles, saved.getRoles());
    }

    @Test
    void registerRejectsInvalidEmailBeforePersisting() {
        RegisterRequest request = new RegisterRequest(
                "not-an-email", "User", "Password1!", Set.of(DPMRole.ROLE_USER));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> authService.register(request));

        assertEquals("Invalid email", exception.getMessage());
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void registerRejectsInvalidPasswordBeforePersisting() {
        RegisterRequest request = new RegisterRequest(
                "user@example.com", "User", "short", Set.of(DPMRole.ROLE_USER));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> authService.register(request));

        assertEquals("Invalid password", exception.getMessage());
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void loginAuthenticatesUserAndIssuesTokens() {
        Set<DPMRole> roles = Set.of(DPMRole.ROLE_USER);
        DPMUser user = user("user@example.com", roles);
        RefreshToken refreshToken = refreshToken("refresh-value");
        when(userRepository.findById("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken("user@example.com", roles)).thenReturn("access-jwt");
        when(refreshTokenService.create("user@example.com")).thenReturn(refreshToken);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(5));

        TokenResponse response = authService.login(
                new AuthRequest("user@example.com", "Password1!"));

        assertEquals("access-jwt", response.accessToken());
        assertEquals("refresh-value", response.refreshToken());
        assertEquals(300, response.expiresIn());
        assertEquals("Bearer", response.tokenType());
        assertEquals("user@example.com", response.username());
        assertEquals(roles, response.dpmRoles());
        verify(refreshTokenService).create("user@example.com");
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findById("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(new AuthRequest("missing@example.com", "Password1!")));

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(passwordEncoder, jwtService, refreshTokenService);
    }

    @Test
    void loginRejectsInvalidPassword() {
        DPMUser user = user("user@example.com", Set.of(DPMRole.ROLE_USER));
        when(userRepository.findById("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(new AuthRequest("user@example.com", "wrong")));

        assertEquals("Invalid credentials", exception.getMessage());
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void refreshValidatesOldTokenIssuesNewPairAndRotatesRefreshToken() {
        Set<DPMRole> roles = Set.of(DPMRole.ROLE_ADMIN);
        RefreshToken current = refreshToken("old-refresh");
        current.setUsername("admin@example.com");
        RefreshToken next = refreshToken("new-refresh");
        next.setUsername("admin@example.com");
        DPMUser user = user("admin@example.com", roles);
        when(refreshTokenService.validate("old-refresh")).thenReturn(current);
        when(userRepository.findById("admin@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("admin@example.com", roles)).thenReturn("new-access-jwt");
        when(refreshTokenService.create("admin@example.com")).thenReturn(next);

        TokenResponse response = authService.refresh("old-refresh");

        assertEquals("new-access-jwt", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals("admin@example.com", response.username());
        assertEquals(roles, response.dpmRoles());
        verify(refreshTokenService).validate("old-refresh");
        verify(refreshTokenService).revoke("old-refresh");
        verify(refreshTokenService).create("admin@example.com");
    }

    @Test
    void refreshPropagatesInvalidRefreshTokenFailure() {
        when(refreshTokenService.validate("invalid"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> authService.refresh("invalid"));

        assertEquals("Invalid refresh token", exception.getMessage());
        verifyNoInteractions(userRepository, jwtService);
    }

    @Test
    void refreshRejectsTokenWhoseUserNoLongerExists() {
        RefreshToken current = refreshToken("refresh-value");
        current.setUsername("deleted@example.com");
        when(refreshTokenService.validate("refresh-value")).thenReturn(current);
        when(userRepository.findById("deleted@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> authService.refresh("refresh-value"));

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(jwtService);
        verify(refreshTokenService, never()).revoke(any());
        verify(refreshTokenService, never()).create(any());
    }

    @Test
    void validateExtractsClaimsAndReturnsValidationResult() {
        Set<DPMRole> roles = Set.of(DPMRole.ROLE_USER);
        when(jwtService.extractUsername("access-jwt")).thenReturn("user@example.com");
        when(jwtService.extractRoles("access-jwt")).thenReturn(roles);
        when(jwtService.validateToken("access-jwt")).thenReturn(true);

        ValidateResponse response = authService.validate("Bearer access-jwt");

        assertEquals(new ValidateResponse(true, "user@example.com", roles), response);
        verify(jwtService).extractUsername("access-jwt");
        verify(jwtService).extractRoles("access-jwt");
        verify(jwtService).validateToken("access-jwt");
    }

    @Test
    void logoutRevokesTheSuppliedRefreshToken() {
        authService.logout("refresh-value");

        verify(refreshTokenService).revoke("refresh-value");
    }

    @Test
    void logoutAllExtractsUserAndRevokesAllUserTokens() {
        DPMUser user = user("user@example.com", Set.of(DPMRole.ROLE_USER));
        when(jwtService.extractUsername("access-jwt")).thenReturn("user@example.com");
        when(jwtService.validateToken("access-jwt")).thenReturn(true);
        when(userRepository.findById("user@example.com")).thenReturn(Optional.of(user));

        authService.logoutAll("Bearer access-jwt");

        verify(jwtService).extractUsername("access-jwt");
        verify(userRepository).findById("user@example.com");
        verify(refreshTokenService).revokeAllForUser("user@example.com");
    }

    @Test
    void logoutAllRejectsUnknownUser() {
        when(jwtService.extractUsername("access-jwt")).thenReturn("missing@example.com");
        when(jwtService.validateToken("access-jwt")).thenReturn(true);
        when(userRepository.findById("missing@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.logoutAll("Bearer access-jwt"));

        assertEquals("User not found", exception.getMessage());
        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    private static DPMUser user(String username, Set<DPMRole> roles) {
        DPMUser user = new DPMUser();
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setRoles(roles);
        return user;
    }

    private static RefreshToken refreshToken(String token) {
        return new RefreshToken(
                "refresh-id", "user@example.com", token,
                Instant.now().plusSeconds(3600), false);
    }
}
