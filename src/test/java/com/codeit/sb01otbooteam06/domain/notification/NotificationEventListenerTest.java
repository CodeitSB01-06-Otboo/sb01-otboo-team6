package com.codeit.sb01otbooteam06.domain.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.follow.entity.Follow;
import com.codeit.sb01otbooteam06.domain.follow.repository.FollowRepository;
import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
import com.codeit.sb01otbooteam06.domain.notification.event.ClothesAttributeAddedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.DirectMessageReceivedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FeedCommentCreatedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FeedLikeCreatedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FolloweeFeedPostedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.NotificationCreateEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.NotificationEventListener;
import com.codeit.sb01otbooteam06.domain.notification.event.UserFollowMeEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.UserRoleChangeEvent;
import com.codeit.sb01otbooteam06.domain.notification.repository.NotificationRepository;
import com.codeit.sb01otbooteam06.domain.notification.util.NotificationCreator;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private FollowRepository followRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private NotificationEventListener listener;

  private final User sender = EntityProvider.createTestUser("sender@example.com", "sender");
  private final User receiver = EntityProvider.createTestUser("receiver@example.com", "receiver");


  @Test
  void handleFeedLiked_다른_사용자에게_알림_생성() {
    // given
    FeedLikeCreatedEvent event = new FeedLikeCreatedEvent(sender, receiver, "좋은 게시글이네요");

    Notification notification = Notification.create(sender, "좋아요 알림", "좋은 게시글이네요",
        NotificationType.INFO);

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {
      mocked.when(() -> NotificationCreator.ofFeedLike(sender, receiver, "좋은 게시글이네요"))
          .thenReturn(notification);

      // when
      listener.handleFeedLiked(event);

      // then
      verify(notificationRepository).save(notification);
      verify(eventPublisher).publishEvent(any(NotificationCreateEvent.class));
    }
  }

  @Test
  void handleFeedLiked_자기자신에게는_알림_생성_안함() {
    // given
    User self = EntityProvider.createTestUser("self@example.com", "self");
    FeedLikeCreatedEvent event = new FeedLikeCreatedEvent(self, self, "자기 댓글");

    // when
    listener.handleFeedLiked(event);

    // then
    verify(notificationRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleUserFollowed_정상적으로_알림이_생성된다() {
    // given
    User follower = EntityProvider.createTestUser("follower@example.com", "팔로워");
    User following = EntityProvider.createTestUser("following@example.com", "팔로잉");

    UserFollowMeEvent event = new UserFollowMeEvent(follower, following);

    Notification notification = Notification.create(
        following,
        "새 팔로워",
        "팔로워님이 당신을 팔로우했습니다.",
        NotificationType.INFO
    );

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {
      mocked.when(() -> NotificationCreator.ofUserFollowMe(follower, following))
          .thenReturn(notification);

      // when
      listener.handleUserFollowed(event);

      // then
      verify(notificationRepository).save(notification);
      verify(eventPublisher).publishEvent(any(NotificationCreateEvent.class));
    }
  }




  @Test
  void handleUserFollowed_같은_사람이면_알림_생성_안함() {
    // given
    User self = EntityProvider.createTestUser("self@example.com", "self");
    UserFollowMeEvent event = new UserFollowMeEvent(self, self);

    // when
    listener.handleUserFollowed(event);

    // then
    verify(notificationRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleClothesAttributeAdded_모든유저에게_알림() {
    // given
    String summary = "여름용 반팔 추천!";
    List<User> users = List.of(sender, receiver);
    when(userRepository.findAll()).thenReturn(users);

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {

      // 각 유저에 대해 Notification 객체를 생성하여 반환
      mocked.when(() -> NotificationCreator.ofClothesAttributeAdded(any(User.class), eq(summary)))
          .thenAnswer(invocation -> {
            User targetUser = invocation.getArgument(0);
            return Notification.create(targetUser, "의류 속성 추가", summary, NotificationType.INFO);
          });

      ClothesAttributeAddedEvent event = new ClothesAttributeAddedEvent(summary);

      // when
      listener.handleClothesAttributeAdded(event);

      // then
      verify(notificationRepository).saveAll(anyList());
      verify(eventPublisher, times(users.size())).publishEvent(any(NotificationCreateEvent.class));
    }
  }

  @Test
  void handleFolloweeFeedPosted_정상작동() {
    // given
    FolloweeFeedPostedEvent event = new FolloweeFeedPostedEvent(receiver, "새 게시글");

    Follow follow1 = mock(Follow.class);
    Follow follow2 = mock(Follow.class);
    User follower1 = EntityProvider.createTestUser("follower1@example.com", "follower1");
    User follower2 = EntityProvider.createTestUser("follower2@example.com", "follower2");

    when(follow1.getFollower()).thenReturn(follower1);
    when(follow2.getFollower()).thenReturn(follower2);
    when(followRepository.findFollowers(eq(receiver.getId()), isNull(), any())).thenReturn(
        List.of(follow1, follow2));

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {
      mocked.when(() -> NotificationCreator.ofFolloweeFeedPosted(any(), any(), any()))
          .thenAnswer(invocation -> {
            User targetUser = invocation.getArgument(0);
            String content = invocation.getArgument(2);
            return Notification.create(targetUser, "팔로우한 유저의 새 피드", content, NotificationType.INFO);
          });

      // when
      listener.handleFolloweeFeedPosted(event);

      // then
      verify(notificationRepository).saveAll(anyList());
      verify(eventPublisher, times(2)).publishEvent(any(NotificationCreateEvent.class));
    }

  }

  @Test
  void handleFeedCommented_다른_사용자에게_알림_생성() {
    // given
    FeedCommentCreatedEvent event = new FeedCommentCreatedEvent(sender, receiver, "댓글입니다!");

    Notification notification = Notification.create(
        receiver,
        "댓글 알림",
        "댓글입니다!",
        NotificationType.INFO
    );

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {
      mocked.when(() -> NotificationCreator.ofFeedComment(sender, receiver, "댓글입니다!"))
          .thenReturn(notification);

      // when
      listener.handleFeedCommented(event);

      // then
      verify(notificationRepository).save(notification);
      verify(eventPublisher).publishEvent(any(NotificationCreateEvent.class));
    }
  }

  @Test
  void handleFeedCommented_자기자신에게는_알림_생성_안함() {
    // given
    User self = EntityProvider.createTestUser("self@example.com", "self");
    FeedCommentCreatedEvent event = new FeedCommentCreatedEvent(self, self, "자기 댓글");

    // when
    listener.handleFeedCommented(event);

    // then
    verify(notificationRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void handleRoleChanged_권한변경알림이_생성된다() {
    // given
    Role previousRole = Role.USER;
    Role newRole = Role.ADMIN;

    User receiver = EntityProvider.createTestUser("receiver@example.com", "receiver");

    UserRoleChangeEvent event = new UserRoleChangeEvent(receiver, previousRole, newRole);

    Notification notification = Notification.create(
        receiver,
        "권한 변경 알림",
        "당신의 권한이 USER에서 CHANNEL_MANAGER로 변경되었습니다.",
        NotificationType.INFO
    );

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {
      mocked.when(() -> NotificationCreator.ofRoleChanged(receiver, previousRole, newRole))
          .thenReturn(notification);

      // when
      listener.handleRoleChanged(event);

      // then
      verify(notificationRepository).save(notification);
      verify(eventPublisher).publishEvent(any(NotificationCreateEvent.class));
    }
  }

  @Test
  void handleDirectMessageReceived_정상적으로_알림이_생성된다() {
    // given
    User sender = EntityProvider.createTestUser("dm_sender@example.com", "보낸이");
    User receiver = EntityProvider.createTestUser("dm_receiver@example.com", "받는이");

    DirectMessageReceivedEvent event =
        new DirectMessageReceivedEvent(sender, receiver, "안녕하세요!");

    Notification notification = Notification.create(
        receiver,
        "새로운 메시지 도착",
        "안녕하세요!",
        NotificationType.INFO
    );

    try (MockedStatic<NotificationCreator> mocked = Mockito.mockStatic(NotificationCreator.class)) {
      mocked.when(() -> NotificationCreator.ofDirectMessageReceive(sender, receiver, "안녕하세요!"))
          .thenReturn(notification);

      // when
      listener.DirectMessageReceived(event);

      // then
      verify(notificationRepository).save(notification);
      verify(eventPublisher).publishEvent(any(NotificationCreateEvent.class));
    }
  }

  @Test
  void handleDirectMessageReceived_자기자신에게_보낸_경우_알림_생성_안함() {
    // given
    User self = EntityProvider.createTestUser("self@example.com", "자기자신");

    DirectMessageReceivedEvent event =
        new DirectMessageReceivedEvent(self, self, "나에게 보내는 메시지");

    // when
    listener.DirectMessageReceived(event);

    // then
    verify(notificationRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
