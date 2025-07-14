package com.codeit.sb01otbooteam06.domain.user.dto;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class UserDto {

    private final UUID id;
    private final Instant createdAt;
    private final String email;
    private final String name;
    private final Role role;
    private final List<String> linkedOAuthProviders;
    private final boolean locked;

    @Builder
    public UserDto(UUID id, Instant createdAt, String email, String name, Role role, List<String> linkedOAuthProviders, boolean locked) {
        this.id = id;
        this.createdAt = createdAt;
        this.email = email;
        this.name = name;
        this.role = role;
        this.linkedOAuthProviders = linkedOAuthProviders != null ? linkedOAuthProviders : Collections.emptyList();
        this.locked = locked;
    }
}

