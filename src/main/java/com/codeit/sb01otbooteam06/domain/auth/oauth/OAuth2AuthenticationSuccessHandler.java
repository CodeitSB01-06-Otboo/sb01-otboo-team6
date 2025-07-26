package com.codeit.sb01otbooteam06.domain.auth.oauth;

import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtTokenProvider;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String provider = oauthToken.getAuthorizedClientRegistrationId(); // e.g., google, kakao
        String providerId = extractProviderId(provider, oAuth2User);

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found for provider: " + provider + ", id: " + providerId));

        UUID userId = user.getId();

        //  User 객체 기반으로 accessToken 생성 (role, name, email 포함)
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // Access Token (일반 쿠키 - JavaScript 접근 가능)
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60); // 1시간

        // Refresh Token (보안 쿠키 - JavaScript 접근 불가)
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        // 프론트엔드로 리디렉션
        response.sendRedirect("http://localhost:5173/oauth/callback");
    }

    private String extractProviderId(String provider, OAuth2User user) {
        Object rawId = user.getAttribute("id");

        System.out.println(">> [OAuth2SuccessHandler] provider = " + provider);
        System.out.println(">> [OAuth2SuccessHandler] rawId = " + rawId);
        System.out.println(">> [OAuth2SuccessHandler] rawId class = " + (rawId != null ? rawId.getClass().getName() : "null"));

        return switch (provider) {
            case "google" -> (String) user.getAttribute("sub");
            case "kakao" -> String.valueOf(rawId); // Long → String 변환
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
