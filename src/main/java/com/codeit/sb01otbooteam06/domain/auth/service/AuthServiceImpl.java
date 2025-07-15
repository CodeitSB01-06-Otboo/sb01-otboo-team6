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

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new TokenResponse(accessToken, refreshToken, user.isMustChangePassword());
    }

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

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UUID)) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        return (UUID) authentication.getPrincipal();
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
