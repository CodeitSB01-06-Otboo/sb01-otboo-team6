package com.codeit.sb01otbooteam06.domain.auth.jwt;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String secretKey = Base64.getEncoder()
            .encodeToString("test-secret-key-long-enough-for-hmac".getBytes());
    private final long accessExp = 3600000;     // 1 hour
    private final long refreshExp = 604800000;  // 7 days

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, accessExp, refreshExp, "test");
    }

    @Test
    void generateAccessToken_withUser_shouldReturnValidToken() {
        User user = User.builder()
                .email("user@example.com")
                .password("encoded-pw")
                .name("테스트유저")
                .role(Role.USER)
                .build();

        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", id);

        String token = jwtTokenProvider.generateAccessToken(user);
        assertThat(token).isNotBlank();

        UUID extractedId = jwtTokenProvider.getUserId(token);
        assertThat(extractedId).isEqualTo(id);
    }

    @Test
    void generateAccessToken_withUUID_shouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);
        assertThat(token).isNotBlank();

        UUID extractedId = jwtTokenProvider.getUserId(token);
        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateRefreshToken(userId);
        assertThat(token).isNotBlank();

        UUID extractedId = jwtTokenProvider.getUserId(token);
        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String fakeToken = "invalid.token.value";
        assertThat(jwtTokenProvider.validateToken(fakeToken)).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }
}
