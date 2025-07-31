//package com.codeit.sb01otbooteam06.domain.user.service;
//
//import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
//import com.codeit.sb01otbooteam06.domain.user.dto.*;
//import com.codeit.sb01otbooteam06.domain.user.entity.Role;
//import com.codeit.sb01otbooteam06.domain.user.entity.User;
//import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
//import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;

//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.*;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//class UserServiceImplTest {
//
//    private UserRepository userRepository;
//    private PasswordEncoder passwordEncoder;
//    private UserServiceImpl userService;
//
//    @BeforeEach
//    void setUp() {
//        userRepository = mock(UserRepository.class);
//        passwordEncoder = new BCryptPasswordEncoder();
//        userService = new UserServiceImpl(userRepository, passwordEncoder);
//    }
//
//    private void setUserId(User user, UUID id) {
//        try {
//            Field field = user.getClass().getSuperclass().getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(user, id);
//        } catch (Exception e) {
//            throw new RuntimeException("ID 설정 실패", e);
//        }
//    }
//
//    @Test
//    @DisplayName("유저 생성 시 프로필도 함께 생성되어야 한다")
//    void testCreateUserWithProfile() {
//        UserCreateRequest request = UserCreateRequest.builder()
//                .email("test@example.com")
//                .password("password123")
//                .name("테스트유저")
//                .build();
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        UserDto result = userService.create(request);
//
//        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//        verify(userRepository).save(captor.capture());
//        User savedUser = captor.getValue();
//
//        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
//        assertThat(passwordEncoder.matches(request.getPassword(), savedUser.getPassword())).isTrue();
//        assertThat(savedUser.getProfile()).isNotNull();
//        assertThat(savedUser.getProfile().getName()).isEqualTo(request.getName());
//        assertThat(savedUser.getProfile().getGender()).isEqualTo(Gender.OTHER);
//        assertThat(savedUser.getProfile().getTemperatureSensitivity()).isEqualTo(3);
//    }
//
//    @Test
//    @DisplayName("중복 이메일로 유저 생성 시 예외 발생")
//    void testCreateUserWithDuplicateEmail() {
//        UserCreateRequest request = UserCreateRequest.builder()
//                .email("duplicate@example.com")
//                .password("password123")
//                .name("중복유저")
//                .build();
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
//
//        assertThrows(IllegalArgumentException.class, () -> userService.create(request));
//        verify(userRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("사용자 역할 변경 시 forceLogout이 true로 설정되어야 한다")
//    void testChangeRole() {
//        User user = User.builder()
//                .email("role@test.com")
//                .password("pw")
//                .name("User")
//                .role(Role.USER)
//                .locked(false)
//                .mustChangePassword(false)
//                .linkedOAuthProviders(new ArrayList<>())
//                .build();
//
//        UUID id = UUID.randomUUID();
//        setUserId(user, id);
//
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        UserRoleUpdateRequest request = new UserRoleUpdateRequest(Role.ADMIN);
//
//        UserDto result = userService.changeRole(id, request);
//
//        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
//        assertThat(user.isForceLogout()).isTrue();
//    }
//
//    @Test
//    @DisplayName("동일한 역할로 변경할 경우 forceLogout이 false 유지")
//    void testChangeRole_NoChange() {
//        User user = User.builder()
//                .email("same@test.com")
//                .password("pw")
//                .name("User")
//                .role(Role.USER)
//                .locked(false)
//                .mustChangePassword(false)
//                .linkedOAuthProviders(new ArrayList<>())
//                .build();
//
//        UUID id = UUID.randomUUID();
//        setUserId(user, id);
//
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        UserDto result = userService.changeRole(id, new UserRoleUpdateRequest(Role.USER));
//
//        assertThat(result.getRole()).isEqualTo(Role.USER);
//        assertThat(user.isForceLogout()).isFalse();
//    }
//
//    @Test
//    @DisplayName("계정 잠금 변경")
//    void testChangeLocked() {
//        User user = User.builder()
//                .email("lock@test.com")
//                .password("pw")
//                .name("User")
//                .role(Role.USER)
//                .locked(false)
//                .mustChangePassword(false)
//                .linkedOAuthProviders(new ArrayList<>())
//                .build();
//
//        UUID id = UUID.randomUUID();
//        setUserId(user, id);
//
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        userService.changeLocked(id, new UserLockUpdateRequest(true));
//
//        assertThat(user.isLocked()).isTrue();
//    }
//
//    @Test
//    @DisplayName("비밀번호 변경")
//    void testChangePassword() {
//        User user = User.builder()
//                .email("pw@test.com")
//                .password("old-encoded")
//                .name("User")
//                .role(Role.USER)
//                .locked(false)
//                .mustChangePassword(false)
//                .linkedOAuthProviders(new ArrayList<>())
//                .build();
//
//        UUID id = UUID.randomUUID();
//        setUserId(user, id);
//
//        when(userRepository.findById(id)).thenReturn(Optional.of(user));
//
//        ChangePasswordRequest request = new ChangePasswordRequest("old", "newPassword123");
//
//        userService.changePassword(id, request);
//
//        assertThat(passwordEncoder.matches("newPassword123", user.getPassword())).isTrue();
//    }
//
//    @Test
//    @DisplayName("사용자 목록 조회 테스트")
//    void testListUsers() {
//        User user = User.builder()
//                .email("user1@example.com")
//                .password("pw1")
//                .name("User1")
//                .role(Role.USER)
//                .locked(false)
//                .linkedOAuthProviders(new ArrayList<>())
//                .build();
//        setUserId(user, UUID.randomUUID());
//
//        when(userRepository.searchUsers(any(), any(), any(), any(), any(), any(), anyInt()))
//                .thenReturn(List.of(user));
//
//        UserListResponse response = userService.list(null, null, 10, "email", "asc", null, null, null);
//
//        assertThat(response.getData()).hasSize(1);
//        assertThat(response.getData().get(0).getEmail()).isEqualTo("user1@example.com");
//        assertThat(response.isHasNext()).isFalse();
//        assertThat(response.getNextCursor()).isNull();
//        assertThat(response.getNextIdAfter()).isNull();
//    }
//
//    @Test
//    @DisplayName("limit 개수만큼 반환되면 hasNext는 true")
//    void testListUsers_HasNext() {
//        List<User> mockUsers = IntStream.range(0, 10)
//                .mapToObj(i -> {
//                    User user = User.builder()
//                            .email("user" + i + "@test.com")
//                            .password("pw")
//                            .name("User" + i)
//                            .role(Role.USER)
//                            .locked(false)
//                            .linkedOAuthProviders(new ArrayList<>())
//                            .build();
//                    setUserId(user, UUID.randomUUID());
//                    return user;
//                }).toList();
//
//        when(userRepository.searchUsers(any(), any(), any(), any(), any(), any(), anyInt()))
//                .thenReturn(mockUsers);
//
//        UserListResponse response = userService.list(null, null, 10, "email", "asc", null, null, null);
//
//        assertThat(response.isHasNext()).isTrue();
//        assertThat(response.getNextCursor()).isNotNull();
//        assertThat(response.getNextIdAfter()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("없는 ID로 비밀번호 변경 시 예외 발생")
//    void testChangePassword_UserNotFound() {
//        UUID id = UUID.randomUUID();
//        when(userRepository.findById(id)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () ->
//                userService.changePassword(id, new ChangePasswordRequest("old", "new")));
//    }
//
//    @Test
//    @DisplayName("없는 ID로 잠금 변경 시 예외 발생")
//    void testChangeLocked_UserNotFound() {
//        UUID id = UUID.randomUUID();
//        when(userRepository.findById(id)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () ->
//                userService.changeLocked(id, new UserLockUpdateRequest(true)));
//    }
//
//    @Test
//    @DisplayName("없는 유저 ID로 역할 변경 시 예외 발생")
//    void testChangeRole_UserNotFound() {
//        UUID id = UUID.randomUUID();
//        when(userRepository.findById(id)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () ->
//                userService.changeRole(id, new UserRoleUpdateRequest(Role.ADMIN)));
//    }
//
//    @Test
//    @DisplayName("linkedOAuthProviders가 null인 경우 toDto에서 빈 리스트로 대체된다")
//    void testToDtoWithNullOAuthProviders() throws Exception {
//        User user = User.builder()
//                .email("nullprovider@example.com")
//                .password("pw")
//                .name("Null")
//                .role(Role.USER)
//                .locked(false)
//                .build(); // linkedOAuthProviders 생략
//
//        setUserId(user, UUID.randomUUID());
//
//        Method toDtoMethod = UserServiceImpl.class.getDeclaredMethod("toDto", User.class);
//        toDtoMethod.setAccessible(true);
//        UserDto dto = (UserDto) toDtoMethod.invoke(userService, user);
//
//        assertThat(dto.getLinkedOAuthProviders()).isNotNull();
//        assertThat(dto.getLinkedOAuthProviders()).isEmpty();
//    }
//}
