package com.depuramente.auth.controller;

import com.depuramente.auth.dto.AuthRequest;
import com.depuramente.auth.dto.LogoutRequest;
import com.depuramente.auth.dto.RegisterRequest;
import com.depuramente.auth.dto.RegisterResponse;
import com.depuramente.auth.dto.TokenResponse;
import com.depuramente.auth.model.DPMRole;
import com.depuramente.auth.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerFunctionalTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private com.depuramente.auth.service.AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsCreatedResponse() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "user@example.com", "User", "Password1!", Set.of(DPMRole.ROLE_USER));
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new RegisterResponse("User", Set.of(DPMRole.ROLE_USER)));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("User"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authService).register(request);
    }

    @Test
    void loginReturnsTokenResponse() throws Exception {
        AuthRequest request = new AuthRequest("user@example.com", "Password1!");
        when(authService.login(any(AuthRequest.class))).thenReturn(tokenResponse());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-value"))
                .andExpect(jsonPath("$.expiresIn").value(300))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("user@example.com"));

        verify(authService).login(request);
    }

    @Test
    void refreshReturnsRotatedTokenResponseAndUsesRefreshTokenFromBody() throws Exception {
        String oldRefreshToken = "old-refresh";
        when(authService.refresh(oldRefreshToken)).thenReturn(tokenResponse());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"attacker@example.com\",\"refreshToken\":\"old-refresh\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-value"))
                .andExpect(jsonPath("$.username").value("user@example.com"));

        verify(authService).refresh(oldRefreshToken);
    }

    @Test
    void validateReturnsTokenValidationResponse() throws Exception {
        when(authService.validate("Bearer access-jwt"))
                .thenReturn(new com.depuramente.auth.dto.ValidateResponse(
                        true, "user@example.com", Set.of(DPMRole.ROLE_USER)));

        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer access-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.username").value("user@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authService).validate("Bearer access-jwt");
    }

    @Test
    void logoutReturnsOkAndRevokesRefreshToken() throws Exception {
        LogoutRequest request = new LogoutRequest("refresh-value");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(authService).logout("refresh-value");
    }

    @Test
    void logoutAllReturnsOkAndUsesAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/auth/logout/all")
                        .header("Authorization", "Bearer access-jwt"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(authService).logoutAll("Bearer access-jwt");
    }

    @Test
    void invalidCredentialsReturnStableFriendlyErrorResponse() throws Exception {
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void revokedRefreshTokenReturnsFriendlyErrorResponse() throws Exception {
        when(authService.refresh("revoked-refresh"))
                .thenThrow(new RuntimeException("Refresh token revoked"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"revoked-refresh\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("The refresh token has been revoked."));
    }

    @Test
    void missingAuthorizationHeaderReturnsFriendlyBadRequest() throws Exception {
        mockMvc.perform(post("/auth/logout/all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("The request is invalid or missing required information."));
    }

    private static TokenResponse tokenResponse() {
        return new TokenResponse(
                "access-jwt", "refresh-value", 300, "Bearer",
                "user@example.com", Set.of(DPMRole.ROLE_USER));
    }
}
