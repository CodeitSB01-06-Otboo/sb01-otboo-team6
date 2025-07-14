package com.codeit.sb01otbooteam06.domain.auth.controller;

import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 관련 API 컨트롤러 (Swagger 명세 기준 완성본)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 - accessToken 반환, refreshToken 쿠키에 저장
     * 성공: 200 OK, accessToken 문자열 반환
     * 실패: 401 Unauthorized, 예외 JSON 반환
     */
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request,
                                    HttpServletResponse response) {

        try {
            TokenResponse tokenResponse = authService.signIn(request);

            ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // 배포 시 true로 변경 필요
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7일
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.ok(tokenResponse.getAccessToken());

        } catch (IllegalArgumentException e) {
            // 비밀번호 불일치 등 실패 케이스
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "SignInFailedException",
                    "message", e.getMessage(),
                    "details", Map.of()
            ));
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "SignInFailedException",
                    "message", "로그인에 실패했습니다.",
                    "details", Map.of()
            ));
        }
    }

    /**
     * 비밀번호 초기화 (임시 비밀번호 발급)
     * 성공: 204 No Content
     * 실패: 404 Not Found, 예외 JSON 반환
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {

        try {
            authService.resetPassword(request);
            return ResponseEntity.noContent().build();

        } catch (com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "exceptionName", "UserNotFoundException",
                    "message", e.getMessage(),
                    "details", Map.of()
            ));
        }
    }

    /**
     * 로그아웃 - refreshToken 쿠키 제거
     * 성공: 204 No Content
     * 실패: 401 Unauthorized, 예외 JSON 반환
     */
    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(@CookieValue(value = "refresh_token", required = false) String refreshToken,
                                     HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "RefreshTokenMissingException",
                    "message", "로그인 정보가 없습니다.",
                    "details", Map.of()
            ));
        }

        // 쿠키 제거
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.noContent().build();
    }


    /**
     * 토큰 재발급 - refreshToken으로 새로운 accessToken 발급
     * 성공: 200 OK, accessToken 반환
     * 실패: 401 Unauthorized, 예외 JSON 반환
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "RefreshTokenMissingException",
                    "message", "리프레시 토큰이 없습니다.",
                    "details", Map.of()
            ));
        }

        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(newAccessToken);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "InvalidRefreshTokenException",
                    "message", e.getMessage(),
                    "details", Map.of()
            ));
        }
    }

    /**
     * 엑세스 토큰 조회 - refreshToken으로 accessToken 반환
     * 성공: 200 OK, accessToken 반환
     * 실패: 401 Unauthorized, 예외 JSON 반환
     */
    @GetMapping("/me")
    public ResponseEntity<?> getAccessToken(@CookieValue(value = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "RefreshTokenMissingException",
                    "message", "리프레시 토큰이 없습니다.",
                    "details", Map.of()
            ));
        }

        try {
            String accessToken = authService.getAccessToken(refreshToken);
            return ResponseEntity.ok(accessToken);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "InvalidRefreshTokenException",
                    "message", e.getMessage(),
                    "details", Map.of()
            ));
        }
    }
    /**
     * CSRF 토큰 조회 API
     * 성공: 200 OK, 토큰 정보 JSON 반환
     * 실패: 401 Unauthorized, 예외 JSON 반환
     */
    @GetMapping("/csrf-token")
    public ResponseEntity<?> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "CsrfTokenMissingException",
                    "message", "CSRF 토큰을 찾을 수 없습니다.",
                    "details", Map.of()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "headerName", csrfToken.getHeaderName(),
                "token", csrfToken.getToken(),
                "parameterName", csrfToken.getParameterName()
        ));
    }
}
