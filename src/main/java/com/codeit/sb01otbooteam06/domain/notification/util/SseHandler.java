package com.codeit.sb01otbooteam06.domain.notification.util;

import com.codeit.sb01otbooteam06.domain.notification.event.FeedCommentCreatedEvent;
import com.codeit.sb01otbooteam06.domain.notification.event.FeedLikeCreatedEvent;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.service.NotificationService;
import com.codeit.sb01otbooteam06.domain.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseHandler {

  private final SseService sseService;
  private final NotificationService notificationService;

  //  피드 좋아요 알림 전송
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(FeedLikeCreatedEvent event) {
    NotificationDto notification = event.notificationDto();
    sseService.send(notification.receiverId(), "notifications", notification);
  }

  // 피드 댓글 알림 전송
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(FeedCommentCreatedEvent event) {
    NotificationDto notification = event.notificationDto();
    sseService.send(notification.receiverId(), "notifications", notification);
  }


}
