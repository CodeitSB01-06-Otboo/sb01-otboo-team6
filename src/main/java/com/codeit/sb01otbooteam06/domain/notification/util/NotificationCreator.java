package com.codeit.sb01otbooteam06.domain.notification.util;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;

public class NotificationCreator {

  //인스턴스화 방지
  private NotificationCreator() {

  }

  public static Notification ofFeedLike(User sender, User receiver, String feedContent) {
    String title = sender.getName() + "님이 피드에 좋아요를 눌렀어요.";
    String content = "\"" + feedContent + "\"";
    return Notification.create(receiver, title, content, NotificationType.INFO);
  }

  public static Notification ofFeedComment(User sender, User receiver, String feedContent) {
    String title = sender.getName() + "님이 댓글을 달았어요.";
    String content = "\"" + feedContent + "\"";
    return Notification.create(receiver, title, content, NotificationType.INFO);
  }

  public static Notification ofRoleChanged(User receiver, Role previousRole, Role newRole) {
    String title = String.format("권한 변경: %s → %s", previousRole.name(), newRole.name());
    String content = String.format("관리자에 의해 권한이 '%s'(으)로 변경되었습니다.", newRole);
    return Notification.create(receiver, title, content, NotificationType.INFO);
  }

  public static Notification ofClothesAttributeAdded(User receiver, String attributeSummary) {
    return Notification.create(receiver, "의상 속성이 추가되었습니다.", attributeSummary,
        NotificationType.INFO);

  }

  public static Notification ofFolloweeFeedPosted(User receiver, User followee, String feedContent) {
    String title = followee.getName() + "님이 새 피드를 등록했어요.";
    return Notification.create(receiver, title, "\"" + feedContent + "\"", NotificationType.INFO);
  }

}
