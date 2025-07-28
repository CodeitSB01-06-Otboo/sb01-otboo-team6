package com.codeit.sb01otbooteam06.domain.notification.event;

import com.codeit.sb01otbooteam06.domain.user.entity.User;

public record UserFollowMeEvent(User follower, User following) {

}
