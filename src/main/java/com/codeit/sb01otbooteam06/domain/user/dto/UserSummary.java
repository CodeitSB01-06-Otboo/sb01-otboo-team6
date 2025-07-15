package com.codeit.sb01otbooteam06.domain.user.dto;

import com.codeit.sb01otbooteam06.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserSummary {

    private UUID userId;
    private String name;
    private String profileImageUrl;

    public static UserSummary from(User user) {
        return UserSummary.builder()
            .userId(user.getId())
            .name(user.getName())
            .profileImageUrl(user.getProfile().getProfileImageUrl())
            .build();
    }
}