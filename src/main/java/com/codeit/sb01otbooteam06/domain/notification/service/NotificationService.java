package com.codeit.sb01otbooteam06.domain.notification.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {


  NotificationDto createNotification(UUID userId, String title, String content,
      NotificationType type);

  public void notifyFeedLiked(User sender, User receiver, String feedContent);

  public void notifyFeedCommented(User sender, User receiver, String feedContent);

  void markAsRead(UUID userId, UUID notificationId);

  PageResponse<NotificationDto> getNotificationsByCursor(UUID userId, Instant cursorCreatedAt,
      UUID cursorId, int limit, String sortBy, String sortDirection);

}
