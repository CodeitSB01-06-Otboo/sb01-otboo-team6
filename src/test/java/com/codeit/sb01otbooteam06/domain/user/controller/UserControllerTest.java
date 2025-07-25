package com.codeit.sb01otbooteam06.domain.user.controller;

import com.codeit.sb01otbooteam06.domain.user.dto.*;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",  // SQL 스크립트 무시
        "spring.jpa.hibernate.ddl-auto=create-drop",  // Hibernate로 테이블 자동 생성
        "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",  // H2를 PostgreSQL 호환 모드로 실행
        "spring.datasource.driverClassName=org.h2.Driver",
        "jwt.secret=oNBHrSfTnlrtuHEGLSnU3bdwtwJIGdsGNPWt0+MlQIA="  // Base64 인코딩된 JWT 시크릿 추가
})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 테스트")
    void testCreateUser() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .name("테스트유저")
                .email("testuser@example.com")
                .password("TestPassword1!")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser@example.com"));

        User saved = userRepository.findByEmail("testuser@example.com").orElseThrow();
        assertThat(saved.getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    void testChangePassword() throws Exception {
        User user = userRepository.save(User.builder()
                .email("changepw@example.com")
                .password(passwordEncoder.encode("oldpass123"))
                .name("비밀번호변경")
                .role(Role.USER)
                .build());

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("oldpass123")
                .newPassword("newpass456")
                .build();

        mockMvc.perform(patch("/api/users/{userId}/password", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newpass456", updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("사용자 권한 변경 테스트")
    void testChangeRole() throws Exception {
        User user = userRepository.save(User.builder()
                .email("changerole@example.com")
                .password("testpass")
                .name("권한유저")
                .role(Role.USER)
                .build());

        UserRoleUpdateRequest request = UserRoleUpdateRequest.builder()
                .role(Role.ADMIN)
                .build();

        mockMvc.perform(patch("/api/users/{userId}/role", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("사용자 잠금 상태 변경 테스트")
    void testChangeLocked() throws Exception {
        User user = userRepository.save(User.builder()
                .email("locktest@example.com")
                .password("pw")
                .name("잠금유저")
                .role(Role.USER)
                .locked(false)
                .build());

        UserLockUpdateRequest request = UserLockUpdateRequest.builder()
                .locked(true)
                .build();

        mockMvc.perform(patch("/api/users/{userId}/lock", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(user.getId().toString()));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.isLocked()).isTrue();
    }

    @Test
    @DisplayName("계정 목록 조회 테스트")
    void testListUsers() throws Exception {
        for (int i = 0; i < 5; i++) {
            userRepository.save(User.builder()
                    .email("user" + i + "@example.com")
                    .password("pw")
                    .name("유저" + i)
                    .role(Role.USER)
                    .build());
        }

        mockMvc.perform(get("/api/users")
                        .param("limit", "3")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.hasNext").value(true));
    }
}
