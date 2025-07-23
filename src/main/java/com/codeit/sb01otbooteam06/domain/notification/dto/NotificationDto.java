package com.codeit.sb01otbooteam06.domain.notification.dto;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationDto(UUID id, String content, boolean isRead, NotificationType type,
                              Instant createdAt) {

  public static NotificationDto from(Notification notification) {
    return new NotificationDto(notification.getId(), notification.getContent(),
        notification.isRead(), notification.getType(), notification.getCreatedAt());
  }

}
