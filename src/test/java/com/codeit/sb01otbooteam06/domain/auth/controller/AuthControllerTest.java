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
        // given
        SignInRequest request = new SignInRequest("login@test.com", "TestPass123");
        TokenResponse response = new TokenResponse("access.jwt.token", "refresh.jwt.token", false);

        reset(authService); // 이전 stub 초기화

        when(authService.signIn(any(SignInRequest.class))).thenReturn(response);

        // when & then
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
        // given
        String expectedAccessToken = "new.access.token";

        // mock 설정
        reset(authService); // 혹시 모를 이전 영향 제거
        when(authService.refreshAccessToken("valid.refresh.token")).thenReturn(expectedAccessToken);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "valid.refresh.token")))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedAccessToken));
    }


    @Test
    @DisplayName("GET /api/auth/me - 액세스 토큰 재발급 성공")
    void testGetAccessTokenSuccess() throws Exception {
        // given
        String expectedAccessToken = "new.access.token";
        String refreshToken = "valid.refresh.token";

        // mock 설정
        reset(authService);
        when(authService.getAccessToken(refreshToken)).thenReturn(expectedAccessToken);

        // when & then
        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedAccessToken));
    }


    @Test
    @DisplayName("GET /api/auth/csrf-token - CSRF 토큰 없을 때 401")
    void testCsrfTokenNotFound() throws Exception {
        mockMvc.perform(get("/api/auth/csrf-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("CsrfTokenMissingException"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 이메일 또는 비밀번호")
    void testSignInFail() throws Exception {
        // given
        SignInRequest request = new SignInRequest("wrong@test.com", "WrongPass");
        reset(authService); // 이전 설정 제거

        when(authService.signIn(any(SignInRequest.class)))
                .thenThrow(new IllegalArgumentException("아이디 또는 비밀번호가 잘못되었습니다."));

        // when & then
        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("SignInFailedException"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 잘못되었습니다."));
    }


    @Test
    @DisplayName("비밀번호 재설정 실패 - 사용자 없음")
    void testResetPasswordUserNotFound() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("notfound@test.com");

        doThrow(new com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException("사용자를 찾을 수 없습니다."))
                .when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionName").value("UserNotFoundException"));
    }

    @Test
    @DisplayName("로그아웃 실패 - 쿠키 없음")
    void testSignOutWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("RefreshTokenMissingException"));
    }

    @Test
    @DisplayName("액세스 토큰 재발급 실패 - 쿠키 없음")
    void testRefreshAccessTokenWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("RefreshTokenMissingException"));
    }

    @Test
    @DisplayName("GET /api/auth/me - 실패 (쿠키 없음)")
    void testGetAccessTokenWithoutCookie() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("RefreshTokenMissingException"));
    }
    @Test
    @DisplayName("로그인 실패 - 서버 내부 예외 발생")
    void testSignInInternalError() throws Exception {
        SignInRequest request = new SignInRequest("error@test.com", "TestPass123");

        when(authService.signIn(any())).thenThrow(new RuntimeException("DB 오류"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("SignInFailedException"))
                .andExpect(jsonPath("$.message").value("로그인에 실패했습니다."));
    }
    @Test
    @DisplayName("리프레시 토큰 재발급 실패 - 유효하지 않은 토큰")
    void testRefreshAccessToken_invalidToken() throws Exception {
        when(authService.refreshAccessToken(anyString()))
                .thenThrow(new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("InvalidRefreshTokenException"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
    }
    @Test
    @DisplayName("GET /api/auth/me - 실패 (유효하지 않은 토큰)")
    void testGetAccessToken_invalidToken() throws Exception {
        when(authService.getAccessToken(anyString()))
                .thenThrow(new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다."));

        mockMvc.perform(get("/api/auth/me")
                        .cookie(new Cookie("refresh_token", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.exceptionName").value("InvalidRefreshTokenException"))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 유효하지 않습니다."));
    }
}