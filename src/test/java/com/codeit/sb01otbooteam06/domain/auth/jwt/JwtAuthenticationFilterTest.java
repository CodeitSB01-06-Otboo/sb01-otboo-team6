package com.codeit.sb01otbooteam06.domain.auth.jwt;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtTokenProvider jwtTokenProvider;
    private UserRepository userRepository;
    private FilterChain filterChain;

    private UUID userId;
    private String validToken;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        userRepository = mock(UserRepository.class);
        filterChain = mock(FilterChain.class);
        filter = new JwtAuthenticationFilter(jwtTokenProvider, userRepository);

        userId = UUID.randomUUID();
        validToken = "valid.jwt.token";
    }

    @Test
    @DisplayName("유효한 토큰 + forceLogout=false -> 인증 통과")
    void validToken_notForceLogout() throws Exception {
        // given
        User user = User.builder()
                .email("user@test.com")
                .password("encoded")
                .name("테스트")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(validToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200); // default
    }

    @Test
    @DisplayName("forceLogout=true -> 인증 거부 + 응답 401")
    void validToken_forceLogoutTrue() throws Exception {
        // given
        User user = User.builder()
                .email("user@test.com")
                .password("encoded")
                .name("테스트")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "forceLogout", true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUserId(validToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("FORCE_LOGOUT");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("토큰 없음 -> 필터 통과")
    void noToken_shouldProceed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 무효 -> 필터 통과")
    void invalidToken_shouldProceed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
