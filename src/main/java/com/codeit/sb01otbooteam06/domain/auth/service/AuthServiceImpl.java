package com.codeit.sb01otbooteam06.domain.auth.service;

import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtTokenProvider;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

/**
 * 인증 서비스 구현체 (Swagger 명세 기반)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인 - accessToken, refreshToken 발급
     */
    @Override
    public TokenResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // [추후 구현] 계정 잠금 상태 확인 로직
        /*
        if (user.isLocked()) {
            throw new IllegalStateException("해당 계정은 잠금 상태입니다.");
        }
        */

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     */
    @Override
    public String refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);
        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        return jwtTokenProvider.generateAccessToken(userId);
    }

    /**
     * 리프레시 토큰으로 현재 액세스 토큰 조회
     */
    @Override
    public String getAccessToken(String refreshToken) {
        return refreshAccessToken(refreshToken);
    }

    /**
     * 비밀번호 초기화 (임시 비밀번호 발급)
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        user.changePassword(passwordEncoder.encode("temporary-password"));

        //  [추후 구현] 이메일 전송 로직
        /*
        String tempPassword = "temporary-password";
        emailService.sendResetPasswordEmail(user.getEmail(), tempPassword);
        */
    }

    /**
     * 내부 메서드 - 리프레시 토큰 검증
     */
    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        return (UUID) authentication.getPrincipal();
    }
}
