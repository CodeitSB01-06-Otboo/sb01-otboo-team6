package com.codeit.sb01otbooteam06.domain.user.service;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.user.dto.UserCreateRequest;
import com.codeit.sb01otbooteam06.domain.user.dto.UserDto;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("유저 생성 시 프로필도 함께 생성되어야 한다")
    void testCreateUserWithProfile() {

        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트유저")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        UserDto result = userService.create(request);


        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(passwordEncoder.matches(request.getPassword(), savedUser.getPassword())).isTrue();
        assertThat(savedUser.getProfile()).isNotNull();
        assertThat(savedUser.getProfile().getName()).isEqualTo(request.getName());
        assertThat(savedUser.getProfile().getGender()).isEqualTo(Gender.OTHER);
        assertThat(savedUser.getProfile().getTemperatureSensitivity()).isEqualTo(3);
    }

    @Test
    @DisplayName("중복 이메일로 유저 생성 시 예외 발생")
    void testCreateUserWithDuplicateEmail() {

        UserCreateRequest request = UserCreateRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .name("중복유저")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);


        assertThrows(IllegalArgumentException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }
}
