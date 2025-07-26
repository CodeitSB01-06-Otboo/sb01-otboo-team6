package com.codeit.sb01otbooteam06.domain.auth.service;

import com.codeit.sb01otbooteam06.domain.auth.dto.MeResponse;
import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtTokenProvider;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    @Transactional
    public TokenResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        boolean matchesMain = passwordEncoder.matches(request.getPassword(), user.getPassword());
        boolean matchesTemp = user.getTemporaryPassword() != null &&
                passwordEncoder.matches(request.getPassword(), user.getTemporaryPassword());

        if (!matchesMain && !matchesTemp) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (matchesTemp) {
            if (user.getTemporaryPasswordExpiration() == null ||
                    user.getTemporaryPasswordExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("임시 비밀번호가 만료되었습니다.");
            }
        }

        if (matchesMain) {
            user.clearTemporaryPassword();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user); //  User 기반 accessToken
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken, user.isMustChangePassword());
    }

    /**
     * 비밀번호 재설정 (임시 비밀번호 발급)
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedTempPassword = passwordEncoder.encode(tempPassword);
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);

        user.changePassword(encodedTempPassword);
        user.setTemporaryPassword(encodedTempPassword, expiration);

        System.out.println("임시 비밀번호 발급됨: " + tempPassword);
    }

    /**
     * 리프레시 토큰으로 accessToken 재발급
     */
    @Override
    public String refreshAccessToken(String refreshToken) {
        return getAccessToken(refreshToken); //  공통 처리
    }

    /**
     * 리프레시 토큰 기반 accessToken 조회 (프론트 /api/auth/me에서 사용)
     */
    @Override
    public String getAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return jwtTokenProvider.generateAccessToken(user); //  role 포함된 JWT 발급
    }

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        return (UUID) authentication.getPrincipal();
    }

    /**
     * 사용자 정보 응답 (선택 API)
     */
    @Override
    public MeResponse getMe(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        UUID userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return new MeResponse(user);
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
