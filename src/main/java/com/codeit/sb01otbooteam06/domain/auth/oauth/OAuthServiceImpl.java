package com.codeit.sb01otbooteam06.domain.auth.oauth;

import com.codeit.sb01otbooteam06.domain.auth.dto.OAuthUserResponse;
import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtTokenProvider;
import com.codeit.sb01otbooteam06.domain.auth.oauth.client.GoogleOAuthClient;
import com.codeit.sb01otbooteam06.domain.auth.oauth.client.KakaoOAuthClient;
import com.codeit.sb01otbooteam06.domain.auth.oauth.userinfo.*;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final GoogleOAuthClient googleOAuthClient;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public OAuthUserResponse getUserInfo(String provider, String code) {
        Map<String, Object> attributes;
        OAuthUserInfo userInfo;

        // provider에 따라 사용자 정보 요청
        if (provider.equalsIgnoreCase("google")) {
            attributes = googleOAuthClient.getUserInfo(code);
            userInfo = new GoogleUserInfo(attributes);
        } else if (provider.equalsIgnoreCase("kakao")) {
            attributes = kakaoOAuthClient.getUserInfo(code);
            userInfo = new KakaoUserInfo(attributes);
        } else {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + provider);
        }

        // 이메일 기준으로 사용자 조회 또는 생성
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    User newUser = User.createSocialUser(userInfo.getEmail(), userInfo.getName());
                    return userRepository.save(newUser);
                });

        // JWT 토큰 발급
        UUID userId = user.getId();
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 응답 객체 반환
        return new OAuthUserResponse(accessToken, refreshToken);
    }
}
