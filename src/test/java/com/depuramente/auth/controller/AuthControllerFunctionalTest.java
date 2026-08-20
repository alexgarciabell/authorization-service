package com.depuramente.auth.controller;

import com.depuramente.auth.dto.AuthRequest;
import com.depuramente.auth.dto.RegisterRequest;
import com.depuramente.auth.dto.RegisterResponse;
import com.depuramente.auth.dto.TokenResponse;
import com.depuramente.auth.model.DPMRole;
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
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
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

    private static TokenResponse tokenResponse() {
        return new TokenResponse(
                "access-jwt", "refresh-value", 300, "Bearer",
                "user@example.com", Set.of(DPMRole.ROLE_USER));
    }
}
