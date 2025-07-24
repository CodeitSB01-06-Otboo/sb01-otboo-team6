package com.codeit.sb01otbooteam06.domain.notification.event;

import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;

public record FeedLikeCreatedEvent(NotificationDto notificationDto) {

}
