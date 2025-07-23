package com.codeit.sb01otbooteam06.domain.auth.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DelegatingOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final GoogleOAuth2UserService googleService;
    private final KakaoOAuth2UserService kakaoService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        String provider = request.getClientRegistration().getRegistrationId();

        return switch (provider) {
            case "google" -> googleService.loadUser(request);
            case "kakao" -> kakaoService.loadUser(request);
            default -> throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        };
    }
}
