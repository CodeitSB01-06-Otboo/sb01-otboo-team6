package com.codeit.sb01otbooteam06.domain.notification.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.event.ClothesAttributeAddedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.DirectMessageReceivedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FeedCommentCreatedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FeedLikeCreatedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FolloweeFeedPostedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.UserFollowMeEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.UserRoleChangeEvent;
import com.codeit.sb01otbooteam06.domain.notification.repository.NotificationQueryRepository;
import com.codeit.sb01otbooteam06.domain.notification.repository.NotificationRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final NotificationQueryRepository notificationQueryRepository;

  @Override
  public void notifyFeedLiked(User sender, User receiver, String feedContent) {
    // 본인 확인 제외
    if (isSelfNotification(sender, receiver)) {
      return;
    }
    eventPublisher.publishEvent(new FeedLikeCreatedEvent(sender, receiver, feedContent));
  }

  @Override
  public void notifyFeedCommented(User sender, User receiver, String feedContent) {
    if (isSelfNotification(sender, receiver)) {
      return;
    }

    eventPublisher.publishEvent(new FeedCommentCreatedEvent(sender, receiver, feedContent));
  }


  @Override
  public void notifyRoleChange(User receiver, Role previousRole, Role newRole) {

    eventPublisher.publishEvent(new UserRoleChangeEvent(receiver, previousRole, newRole));
  }

  @Override
  public void notifyClothesAttributeAdded(String attributeSummary) {

    eventPublisher.publishEvent(new ClothesAttributeAddedEvent(attributeSummary));

  }

  @Override
  public void notifyFolloweePostedFeed(User followee, String feedContent) {

    eventPublisher.publishEvent(new FolloweeFeedPostedEvent(followee, feedContent));

  }

  @Override
  public void notifyUserFollowed(User follower, User following) {
    if (follower.getId().equals(following.getId())) return;

    eventPublisher.publishEvent(new UserFollowMeEvent(follower, following));
  }

  @Override
  public void notifyDirectMessage(User sender, User receiver, String messageContent) {
    if (isSelfNotification(sender, receiver)) return;

    eventPublisher.publishEvent(new DirectMessageReceivedEvent(sender, receiver, messageContent));
  }

  @Transactional
  @Override
  public void markAsRead(UUID userId, UUID notificationId) {
    //todo : 나중에 알림 커스텀 에러 구현해서 리팩토링
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new OtbooException(ErrorCode.ILLEGAL_ARGUMENT_ERROR));

    //이 알림이 현재 로그인한 사용자의 것이 아니라면 접근을 차단한다
    if (!notification.getUser().getId().equals(userId)) {
      throw new OtbooException(ErrorCode.ILLEGAL_ARGUMENT_ERROR);
    }
    notification.markAsRead();
  }

  @Override
  public PageResponse<NotificationDto> getNotificationsByCursor(UUID userId,
      Instant cursorCreatedAt, UUID cursorId, int limit, String sortBy, String sortDirection) {

    Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection)
        .orElse(Sort.Direction.DESC);
    Pageable pageable = PageRequest.of(0, limit,
        Sort.by(direction, "createdAt").and(Sort.by(direction, "id")));

    List<Notification> notifications = notificationQueryRepository.findUnreadByUserIdWithCursorPagination(
        userId, cursorCreatedAt, cursorId, limit);

    List<NotificationDto> data = notifications.stream().map(NotificationDto::from).toList();

    boolean hasNext = data.size() == limit;
    String nextCursor = hasNext ? data.get(data.size() - 1).createdAt().toString() : null;
    String nextIdAfter = hasNext ? data.get(data.size() - 1).id().toString() : null;

    int totalCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

    return new PageResponse<>(data, nextCursor, nextIdAfter, hasNext, totalCount, sortBy,
        sortDirection);
  }

  // 본인 알림 인지 확인하는 메서드
  private boolean isSelfNotification(User sender, User receiver) {
    return sender.getId().equals(receiver.getId());
  }
}
