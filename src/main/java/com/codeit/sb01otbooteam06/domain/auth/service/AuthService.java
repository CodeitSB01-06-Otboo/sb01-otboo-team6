package com.codeit.sb01otbooteam06.domain.auth.service;

import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import java.util.UUID;

public interface AuthService {

    /**
     * 로그인 - accessToken, refreshToken 발급
     */
    TokenResponse signIn(SignInRequest request);

    /**
     * 비밀번호 초기화 (임시 비밀번호 발급)
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     */
    String refreshAccessToken(String refreshToken);

    /**
     * 리프레시 토큰으로 현재 액세스 토큰 조회
     */
    String getAccessToken(String refreshToken);

    /**
     * 현재 로그인한 사용자의 UUID를 반환합니다.
     * @return UUID - 인증된 사용자 ID
     */
    UUID getCurrentUserId();
}
