package com.codeit.sb01otbooteam06.domain.notification.event;

import com.codeit.sb01otbooteam06.domain.user.entity.User;

public record DirectMessageReceivedEvent(User sender, User receiver, String messageContent) {

}
