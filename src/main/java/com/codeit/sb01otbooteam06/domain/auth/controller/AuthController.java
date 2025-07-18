package com.codeit.sb01otbooteam06.domain.auth.controller;

import com.codeit.sb01otbooteam06.domain.auth.dto.OAuthUserResponse;
import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import com.codeit.sb01otbooteam06.domain.auth.oauth.OAuthService;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request,
                                    HttpServletResponse response) {
        try {
            TokenResponse tokenResponse = authService.signIn(request);

            ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.ok(tokenResponse.getAccessToken());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "SignInFailedException",
                    "message", e.getMessage(),
                    "details", Map.of()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "exceptionName", "SignInFailedException",
                    "message", "로그인에 실패했습니다.",
                    "details", Map.of()
            ));
        }
    }

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

        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.noContent().build();
    }

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

    @GetMapping("/oauth/callback/{provider}")
    public ResponseEntity<?> oauthCallback(@PathVariable String provider,
                                           @RequestParam String code) {
        try {
            OAuthUserResponse userResponse = oAuthService.getUserInfo(provider, code);
            return ResponseEntity.ok(userResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exceptionName", "OAuthProviderException",
                    "message", e.getMessage(),
                    "details", Map.of()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "exceptionName", "OAuthCallbackException",
                    "message", "소셜 로그인 처리 중 오류가 발생했습니다.",
                    "details", Map.of()
            ));
        }
    }
}
