package com.codeit.sb01otbooteam06.domain.notification.event;

import com.codeit.sb01otbooteam06.domain.user.entity.User;

public record FolloweeFeedPostedEvent(User followee, String feedContent) {

}
