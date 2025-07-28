package com.codeit.sb01otbooteam06.domain.auth.dto;

import com.codeit.sb01otbooteam06.domain.user.entity.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MeResponse {
    private final UUID id;
    private final String email;
    private final String role;

    public MeResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole().name(); // Enum → String
    }
}
