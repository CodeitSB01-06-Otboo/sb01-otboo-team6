package com.codeit.sb01otbooteam06.domain.notification.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, name = "title",columnDefinition = "TEXT")
  private String title;

  @Column(nullable = false, name = "content",columnDefinition = "TEXT")
  private String content;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "type")
  private NotificationType type;

  // 생성자 메서드
  public static Notification create(User user, String title ,String content, NotificationType type) {
    Notification notification = new Notification();
    notification.user = user;
    notification.title = title;
    notification.content = content;
    notification.type = type;
    notification.isRead = false;
    return notification;
  }

  // 읽음 표시
  public void markAsRead() {
    this.isRead = true;
  }

}
