package com.codeit.sb01otbooteam06.domain.notification.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationCreator 테스트")
class NotificationCreatorTest {

  private final User sender = EntityProvider.createTestUser("alice@test.com", "Alice");
  private final User receiver = EntityProvider.createTestUser("bob@test.com", "Bob");

  @Test
  void ofFeedLike_정상생성() {
    Notification notification = NotificationCreator.ofFeedLike(sender, receiver, "좋은 날씨네요!");

    assertEquals(receiver, notification.getUser());
    assertEquals("Alice님이 피드에 좋아요를 눌렀어요.", notification.getTitle());
    assertEquals("\"좋은 날씨네요!\"", notification.getContent());
    assertEquals(NotificationType.INFO, notification.getType());
  }

  @Test
  void ofFeedComment_정상생성() {
    Notification notification = NotificationCreator.ofFeedComment(sender, receiver, "멋진 사진이에요!");

    assertEquals(receiver, notification.getUser());
    assertEquals("Alice님이 댓글을 달았어요.", notification.getTitle());
    assertEquals("\"멋진 사진이에요!\"", notification.getContent());
  }

  @Test
  void ofRoleChanged_정상생성() {
    Notification notification = NotificationCreator.ofRoleChanged(receiver, Role.USER, Role.ADMIN);

    assertEquals(receiver, notification.getUser());
    assertEquals("권한 변경: USER → ADMIN", notification.getTitle());
    assertEquals("관리자에 의해 권한이 'ADMIN'(으)로 변경되었습니다.", notification.getContent());
  }

  @Test
  void ofClothesAttributeAdded_정상생성() {
    String summary = "색상: 파랑, 계절: 여름";
    Notification notification = NotificationCreator.ofClothesAttributeAdded(receiver, summary);

    assertEquals("의상 속성이 추가되었습니다.", notification.getTitle());
    assertEquals(summary, notification.getContent());
  }

  @Test
  void ofFolloweeFeedPosted_정상생성() {
    Notification notification = NotificationCreator.ofFolloweeFeedPosted(receiver, sender, "새로운 OOTD!");

    assertEquals("Alice님이 새 피드를 등록했어요.", notification.getTitle());
    assertEquals("\"새로운 OOTD!\"", notification.getContent());
  }

  @Test
  void ofUserFollowMe_정상생성() {
    Notification notification = NotificationCreator.ofUserFollowMe(sender, receiver);

    assertEquals("Alice님이 나를 팔로우하기 시작했어요.", notification.getTitle());
    assertEquals("", notification.getContent());
    assertEquals(receiver, notification.getUser());
  }

  @Test
  void ofDirectMessageReceive_정상생성() {
    Notification notification = NotificationCreator.ofDirectMessageReceive(sender, receiver, "안녕하세요");

    assertEquals("Alice님으로부터 새 메시지가 도착했어요.", notification.getTitle());
    assertEquals("\"안녕하세요\"", notification.getContent());
  }
}
