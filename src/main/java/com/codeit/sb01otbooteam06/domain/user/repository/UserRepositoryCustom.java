package com.codeit.sb01otbooteam06.domain.user.repository;

import com.codeit.sb01otbooteam06.domain.user.entity.User;

import java.util.List;

public interface UserRepositoryCustom {

    List<User> searchUsers(
            String cursor,
            String emailLike,
            String roleEqual,
            Boolean locked,
            String sortBy,
            String sortDirection,
            int limit
    );
}
