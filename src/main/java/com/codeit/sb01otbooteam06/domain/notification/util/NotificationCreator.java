package com.codeit.sb01otbooteam06.domain.notification.util;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.NotificationType;
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

}
