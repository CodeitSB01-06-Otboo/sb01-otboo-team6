package com.codeit.sb01otbooteam06.domain.auth.controller;

import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }

        @Bean
        public AuthController authController(AuthService authService) {
            return new AuthController(authService);
        }

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    @DisplayName("로그인 성공")
    void testSignInSuccess() throws Exception {
        SignInRequest request = new SignInRequest("login@test.com", "TestPass123");
        TokenResponse response = new TokenResponse("access.jwt.token", "refresh.jwt.token", false);

        when(authService.signIn(any(SignInRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("access.jwt.token"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void testResetPasswordSuccess() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset@test.com");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("로그아웃 성공 시 쿠키 삭제")
    void testSignOutSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(new Cookie("refresh_token", "valid.refresh.token")))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("액세스 토큰 재발급 - 성공")
    void testRefreshAccessTokenSuccess() throws Exception {
        when(authService.refreshAccessToken(anyString())).thenReturn("new.access.token");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "valid.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(content().string("new.access.token"));
    }

    @Test
    @DisplayName("GET /api/auth/me - 액세스 토큰 재발급 성공")
    void testGetAccessTokenSuccess() throws Exception {
        when(authService.getAccessToken(anyString())).thenReturn("new.access.token");

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("refresh_token", "valid.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(content().string("new.access.token"));
    }

    @Test
    @DisplayName("GET /api/auth/csrf-token - CSRF 토큰 없을 때 401")
    void testCsrfTokenNotFound() throws Exception {
        mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("CsrfTokenMissingException"));
    }
}
