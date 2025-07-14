package com.codeit.sb01otbooteam06.domain.user.dto;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequest {

    private Role role;
}