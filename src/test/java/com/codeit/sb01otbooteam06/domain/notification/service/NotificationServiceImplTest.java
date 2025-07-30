package com.codeit.sb01otbooteam06.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
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
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationQueryRepository notificationQueryRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  private User sender;
  private User receiver;
  private UUID userId;
  private UUID notificationId;

  @BeforeEach
  void setUp() {
    sender = EntityProvider.createTestUser("sender@example.com", "보낸이");
    receiver = EntityProvider.createTestUser("receiver@example.com", "받는이");
    userId = receiver.getId();
    notificationId = UUID.randomUUID();
  }

  @Test
  void notifyFeedLiked_shouldPublishEvent_whenNotSelfNotification() {
    notificationService.notifyFeedLiked(sender, receiver, "피드 내용");
    verify(eventPublisher).publishEvent(any(FeedLikeCreatedEvent.class));
  }

  @Test
  void notifyFeedLiked_shouldNotPublishEvent_whenSelfNotification() {
    notificationService.notifyFeedLiked(sender, sender, "피드 내용");
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void notifyDirectMessage_shouldPublishEvent_whenDifferentUsers() {
    notificationService.notifyDirectMessage(sender, receiver, "메시지 내용");
    verify(eventPublisher).publishEvent(any(DirectMessageReceivedEvent.class));
  }

  @Test
  void notifyUserFollowed_shouldPublishEvent_whenDifferentUsers() {
    notificationService.notifyUserFollowed(sender, receiver);
    verify(eventPublisher).publishEvent(any(UserFollowMeEvent.class));
  }

  @Test
  void notifyUserFollowed_shouldNotPublishEvent_whenSelfFollow() {
    notificationService.notifyUserFollowed(sender, sender);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void markAsRead_shouldUpdateReadStatus_whenUserOwnsNotification() {
    Notification notification = Notification.create(receiver, "제목", "내용", NotificationType.INFO);

    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

    notificationService.markAsRead(userId, notificationId);

    assertTrue(notification.isRead());
  }

  @Test
  void markAsRead_shouldThrowException_whenNotificationDoesNotExist() {
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

    assertThrows(OtbooException.class, () ->
        notificationService.markAsRead(userId, notificationId));
  }

  @Test
  void markAsRead_shouldThrowException_whenUserDoesNotOwnNotification() {
    Notification notification = Notification.create(sender, "제목", "내용", NotificationType.INFO);

    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

    assertThrows(OtbooException.class, () ->
        notificationService.markAsRead(userId, notificationId));
  }

  @Test
  void getNotificationsByCursor_shouldReturnPagedResponse() {
    Instant cursorCreatedAt = Instant.now();
    UUID cursorId = UUID.randomUUID();
    int limit = 2;

    Notification n1 = Notification.create(receiver, "title1", "content1", NotificationType.INFO);
    Notification n2 = Notification.create(receiver, "title2", "content2", NotificationType.INFO);
    // createdAt 및 id 수동 설정
    setBaseEntityFields(n1, UUID.randomUUID(), cursorCreatedAt.minusSeconds(10));
    setBaseEntityFields(n2, UUID.randomUUID(), cursorCreatedAt.minusSeconds(5));

    when(notificationQueryRepository.findUnreadByUserIdWithCursorPagination(
        eq(userId), eq(cursorCreatedAt), eq(cursorId), eq(limit))
    ).thenReturn(List.of(n1, n2));

    when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(10);

    PageResponse<NotificationDto> result = notificationService.getNotificationsByCursor(
        userId, cursorCreatedAt, cursorId, limit, "createdAt", "DESC"
    );

    assertEquals(2, result.getData().size());
    assertTrue(result.isHasNext());
    assertEquals(10, result.getTotalCount());
    assertEquals("createdAt", result.getSortBy());
    assertEquals("DESC", result.getSortDirection());
  }

  @Test
  void notifyFeedCommented_shouldPublishEvent_whenNotSelfNotification() {
    String feedContent = "댓글이 달린 피드입니다";

    notificationService.notifyFeedCommented(sender, receiver, feedContent);

    ArgumentCaptor<FeedCommentCreatedEvent> captor = ArgumentCaptor.forClass(FeedCommentCreatedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    FeedCommentCreatedEvent event = captor.getValue();
    assertEquals(sender, event.sender());
    assertEquals(receiver, event.receiver());
    assertEquals(feedContent, event.feedContent());
  }

  @Test
  void notifyFeedCommented_shouldNotPublishEvent_whenSelfNotification() {
    notificationService.notifyFeedCommented(sender, sender, "자기 댓글");

    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void notifyRoleChange_shouldPublishEvent() {
    Role oldRole = Role.USER;
    Role newRole = Role.ADMIN;

    notificationService.notifyRoleChange(receiver, oldRole, newRole);

    ArgumentCaptor<UserRoleChangeEvent> captor = ArgumentCaptor.forClass(UserRoleChangeEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    UserRoleChangeEvent event = captor.getValue();
    assertEquals(receiver, event.receiver());
    assertEquals(oldRole, event.previousRole());
    assertEquals(newRole, event.newRole());
  }

  @Test
  void notifyClothesAttributeAdded_shouldPublishEvent() {
    String summary = "새로운 속성: 방수";

    notificationService.notifyClothesAttributeAdded(summary);

    ArgumentCaptor<ClothesAttributeAddedEvent> captor = ArgumentCaptor.forClass(ClothesAttributeAddedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    ClothesAttributeAddedEvent event = captor.getValue();
    assertEquals(summary, event.attributeSummary());
  }

  @Test
  void notifyFolloweePostedFeed_shouldPublishEvent() {
    String feedContent = "팔로우한 사람이 새 피드를 올렸습니다.";

    notificationService.notifyFolloweePostedFeed(sender, feedContent);

    ArgumentCaptor<FolloweeFeedPostedEvent> captor = ArgumentCaptor.forClass(FolloweeFeedPostedEvent.class);
    verify(eventPublisher).publishEvent(captor.capture());

    FolloweeFeedPostedEvent event = captor.getValue();
    assertEquals(sender, event.followee());
    assertEquals(feedContent, event.feedContent());
  }

  @Test
  void getNotificationsByCursor_shouldSetHasNextTrue_whenSizeEqualsLimit() {
    int limit = 2;
    Instant cursorCreatedAt = Instant.now();
    UUID cursorId = UUID.randomUUID();

    Notification n1 = Notification.create(receiver, "title1", "content1", NotificationType.INFO);
    Notification n2 = Notification.create(receiver, "title2", "content2", NotificationType.INFO);
    setBaseEntityFields(n1, UUID.randomUUID(), cursorCreatedAt.minusSeconds(10));
    setBaseEntityFields(n2, UUID.randomUUID(), cursorCreatedAt.minusSeconds(5));

    when(notificationQueryRepository.findUnreadByUserIdWithCursorPagination(
        eq(userId), eq(cursorCreatedAt), eq(cursorId), eq(limit)))
        .thenReturn(List.of(n1, n2));

    when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(10);

    PageResponse<NotificationDto> result = notificationService.getNotificationsByCursor(
        userId, cursorCreatedAt, cursorId, limit, "createdAt", "DESC");

    assertTrue(result.isHasNext());
    assertNotNull(result.getNextCursor());
    assertNotNull(result.getNextIdAfter());
  }

  @Test
  void getNotificationsByCursor_shouldSetHasNextFalse_whenSizeLessThanLimit() {
    int limit = 2;
    Instant cursorCreatedAt = Instant.now();
    UUID cursorId = UUID.randomUUID();

    Notification n1 = Notification.create(receiver, "title1", "content1", NotificationType.INFO);
    setBaseEntityFields(n1, UUID.randomUUID(), cursorCreatedAt.minusSeconds(10));

    when(notificationQueryRepository.findUnreadByUserIdWithCursorPagination(
        eq(userId), eq(cursorCreatedAt), eq(cursorId), eq(limit)))
        .thenReturn(List.of(n1)); // size = 1 < limit

    when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(1);

    PageResponse<NotificationDto> result = notificationService.getNotificationsByCursor(
        userId, cursorCreatedAt, cursorId, limit, "createdAt", "DESC");

    assertFalse(result.isHasNext());
    assertNull(result.getNextCursor());
    assertNull(result.getNextIdAfter());
  }

  private void setBaseEntityFields(Notification notification, UUID id, Instant createdAt) {
    try {
      Field idField = BaseEntity.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(notification, id);

      Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(notification, createdAt);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
