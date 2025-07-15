package com.codeit.sb01otbooteam06.domain.follow.dto;

import com.codeit.sb01otbooteam06.domain.follow.entity.Follow;
import com.codeit.sb01otbooteam06.domain.user.dto.UserSummary;
import java.util.UUID;

public record FollowDto(
    UUID id,
    UserSummary follower,
    UserSummary followee) {

    public static FollowDto from(Follow f) {
        return new FollowDto(
            f.getId(),
            UserSummary.from(f.getFollower()),
            UserSummary.from(f.getFollowee()));
    }
}