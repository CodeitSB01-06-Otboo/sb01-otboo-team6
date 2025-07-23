package com.codeit.sb01otbooteam06.domain.notification.dto;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(UUID id, Instant createdAt, UUID receiverId, String title,
                              String content, NotificationType level) {

  public static NotificationDto from(Notification notification) {
    User receiver = notification.getUser();
    return new NotificationDto(notification.getId(), notification.getCreatedAt(), receiver.getId(),
        notification.getTitle(), notification.getContent(), notification.getType());

  }

}
