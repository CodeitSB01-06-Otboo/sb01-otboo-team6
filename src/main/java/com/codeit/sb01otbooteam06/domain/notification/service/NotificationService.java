package com.codeit.sb01otbooteam06.domain.notification.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.time.Instant;
import java.util.UUID;

public interface NotificationService {

  public void notifyRoleChange(User receiver, Role previousRole, Role newRole);

  public void notifyFeedLiked(User sender, User receiver, String feedContent);

  public void notifyFeedCommented(User sender, User receiver, String feedContent);

  public void notifyClothesAttributeAdded(User receiver, String attributeSummary);

  public void notifyFolloweePostedFeed(User followee, String feedContent);

  void markAsRead(UUID userId, UUID notificationId);

  PageResponse<NotificationDto> getNotificationsByCursor(UUID userId, Instant cursorCreatedAt,
      UUID cursorId, int limit, String sortBy, String sortDirection);

}
