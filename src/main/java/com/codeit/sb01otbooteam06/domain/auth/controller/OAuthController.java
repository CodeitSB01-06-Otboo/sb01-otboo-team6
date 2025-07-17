package com.codeit.sb01otbooteam06.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RestController
@RequestMapping("/oauth2/authorization")
@RequiredArgsConstructor
public class OAuthController {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @GetMapping("/{provider}")
    public void redirectToProvider(@PathVariable String provider,
                                   HttpServletResponse response) throws IOException {
        String redirectUrl;

        switch (provider.toLowerCase()) {
            case "google":
                redirectUrl = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                        .queryParam("client_id", googleClientId)
                        .queryParam("redirect_uri", googleRedirectUri)
                        .queryParam("response_type", "code")
                        .queryParam("scope", "email profile")
                        .queryParam("access_type", "offline")
                        .queryParam("prompt", "consent")
                        .build()
                        .toUriString();
                break;

            case "kakao":
                redirectUrl = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                        .queryParam("client_id", kakaoClientId)
                        .queryParam("redirect_uri", kakaoRedirectUri)
                        .queryParam("response_type", "code")
                        .build()
                        .toUriString();
                break;

            default:
                throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        }

        response.sendRedirect(redirectUrl);
    }
}
