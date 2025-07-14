package com.codeit.sb01otbooteam06.domain.user.service;

import com.codeit.sb01otbooteam06.domain.user.dto.*;

import java.util.UUID;

public interface UserService {

    UserDto create(UserCreateRequest request);

    UserDto changeRole(UUID userId, UserRoleUpdateRequest request);

    void changeLocked(UUID userId, UserLockUpdateRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);

    UserListResponse list(
            String cursor,
            UUID idAfter,
            int limit,
            String sortBy,
            String sortDirection,
            String emailLike,
            String roleEqual,
            Boolean locked
    );
}
