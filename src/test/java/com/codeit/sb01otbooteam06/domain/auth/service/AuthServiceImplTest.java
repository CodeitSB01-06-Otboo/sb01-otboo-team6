package com.codeit.sb01otbooteam06.domain.auth.service;

import com.codeit.sb01otbooteam06.domain.auth.dto.MeResponse;
import com.codeit.sb01otbooteam06.domain.auth.dto.ResetPasswordRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.SignInRequest;
import com.codeit.sb01otbooteam06.domain.auth.dto.TokenResponse;
import com.codeit.sb01otbooteam06.domain.auth.jwt.JwtTokenProvider;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("로그인 성공 - 메인 비밀번호 일치")
    void signIn_success_with_main_password() {
        SignInRequest request = new SignInRequest("user@example.com", "password123");
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(user.getId()).willReturn(userId);
        given(user.isMustChangePassword()).willReturn(false);
        given(jwtTokenProvider.generateAccessToken(user)).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(userId)).willReturn("refresh-token");

        TokenResponse response = authService.signIn(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.isMustChangePassword()).isFalse();

    }


    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void signIn_fail_invalid_password() {
        SignInRequest request = new SignInRequest("user@example.com", "wrongpass");
        User user = mock(User.class);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);
        given(user.getTemporaryPassword()).willReturn(null);

        assertThatThrownBy(() -> authService.signIn(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 - 임시 비밀번호 생성됨")
    void resetPassword_success() {
        ResetPasswordRequest request = new ResetPasswordRequest("user@example.com");
        User user = mock(User.class);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.encode(anyString())).willReturn("encodedTemp");

        authService.resetPassword(request);

        then(user).should().changePassword("encodedTemp");
        then(user).should().setTemporaryPassword(eq("encodedTemp"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("accessToken 재발급 성공")
    void refreshAccessToken_success() {
        String refreshToken = "valid-refresh-token";
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(user)).willReturn("new-access");

        String token = authService.refreshAccessToken(refreshToken);

        assertThat(token).isEqualTo("new-access");
    }

    @Test
    @DisplayName("현재 사용자 ID 조회 실패 - 인증 객체 없음")
    void getCurrentUserId_fail() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authService.getCurrentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("인증된 사용자 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("현재 사용자 ID 조회 성공")
    void getCurrentUserId_success() {
        UUID userId = UUID.randomUUID();

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userId);

        SecurityContext context = mock(SecurityContext.class);
        given(context.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(context);

        UUID result = authService.getCurrentUserId();

        assertThat(result).isEqualTo(userId);
    }

    @Test
    @DisplayName("getMe - 유효한 리프레시 토큰으로 사용자 정보 반환")
    void getMe_success() {
        // given
        String refreshToken = "valid-refresh-token";
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 💡 필요한 Mock 설정
        given(user.getId()).willReturn(userId);
        given(user.getEmail()).willReturn("user@example.com");
        given(user.getRole()).willReturn(com.codeit.sb01otbooteam06.domain.user.entity.Role.USER); // ⭐ 중요

        // when
        MeResponse me = authService.getMe(refreshToken);

        // then
        assertThat(me).isNotNull();
        assertThat(me.getId()).isEqualTo(userId);
        assertThat(me.getEmail()).isEqualTo("user@example.com");
        assertThat(me.getRole()).isEqualTo("USER");
    }



    @Test
    @DisplayName("getMe - 유효하지 않은 리프레시 토큰")
    void getMe_invalidToken() {
        String refreshToken = "invalid-token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(false);

        assertThatThrownBy(() -> authService.getMe(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void signIn_fail_user_not_found() {
        SignInRequest request = new SignInRequest("notfound@example.com", "password");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signIn(request))
                .isInstanceOf(UserNotFoundException.class)
                .satisfies(ex -> assertThat(ex.getCause().getMessage()).contains("notfound@example.com"));
    }
    @Test
    @DisplayName("로그인 성공 - 임시 비밀번호 사용")
    void signIn_tempPassword_success() {
        SignInRequest request = new SignInRequest("user@example.com", "temp1234");
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);
        given(user.getTemporaryPassword()).willReturn("encodedTemp");
        given(passwordEncoder.matches(request.getPassword(), "encodedTemp")).willReturn(true);
        given(user.getTemporaryPasswordExpiration()).willReturn(LocalDateTime.now().plusMinutes(5));
        given(user.getId()).willReturn(userId);
        given(user.isMustChangePassword()).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(user)).willReturn("access");
        given(jwtTokenProvider.generateRefreshToken(userId)).willReturn("refresh");

        TokenResponse response = authService.signIn(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("로그인 실패 - 임시 비밀번호 만료")
    void signIn_fail_tempPassword_expired() {
        SignInRequest request = new SignInRequest("user@example.com", "temp1234");
        User user = mock(User.class);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);
        given(user.getTemporaryPassword()).willReturn("encodedTemp");
        given(passwordEncoder.matches(request.getPassword(), "encodedTemp")).willReturn(true);
        given(user.getTemporaryPasswordExpiration()).willReturn(LocalDateTime.now().minusMinutes(1));

        assertThatThrownBy(() -> authService.signIn(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("임시 비밀번호가 만료되었습니다.");
    }

    @Test
    @DisplayName("refreshToken 유효성 검사 실패")
    void validateRefreshToken_invalid() {
        String token = "invalid";
        given(jwtTokenProvider.validateToken(token)).willReturn(false);

        assertThatThrownBy(() -> authService.getAccessToken(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 리프레시 토큰");
    }

}
