package com.codeit.sb01otbooteam06.domain.user.service;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.dto.*;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.USER)
                .locked(false)
                .linkedOAuthProviders(new ArrayList<>())
                .build();

        Profile profile = new Profile(
                user,
                user.getName(),
                Gender.OTHER,
                LocalDate.now(),
                0.0,
                0.0,
                0,
                0,
                List.of(),
                3,
                null
        );

        user.setProfile(profile);  // User에 Profile 세팅

        userRepository.save(user); // Profile까지 함께 저장됨 (Cascade)

        return toDto(user);
    }

    @Override
    @Transactional
    public UserDto changeRole(UUID userId, UserRoleUpdateRequest request) {
        User user = findById(userId);
        user.changeRole(request.getRole());
        return toDto(user);
    }

    @Override
    @Transactional
    public void changeLocked(UUID userId, UserLockUpdateRequest request) {
        User user = findById(userId);
        user.changeLocked(request.isLocked());
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findById(userId);
        user.changePassword(passwordEncoder.encode(request.getPassword()));
    }

    @Override
    public UserListResponse list(String cursor, UUID idAfter, int limit, String sortBy, String sortDirection, String emailLike, String roleEqual, Boolean locked) {

        List<User> users = userRepository.searchUsers(cursor, emailLike, roleEqual, locked, sortBy, sortDirection, limit);

        List<UserDto> content = users.stream()
                .map(this::toDto)
                .toList();

        boolean hasNext = users.size() == limit;
        String nextCursor = hasNext ? users.get(users.size() - 1).getId().toString() : null;
        UUID nextIdAfter = hasNext ? users.get(users.size() - 1).getId() : null;

        return UserListResponse.builder()
                .data(content)
                .nextCursor(nextCursor)
                .nextIdAfter(nextIdAfter)
                .hasNext(hasNext)
                .totalCount(users.size())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }

    private User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .linkedOAuthProviders(user.getLinkedOAuthProviders() == null ? new ArrayList<>() : user.getLinkedOAuthProviders())
                .locked(user.isLocked())
                .build();
    }
}
