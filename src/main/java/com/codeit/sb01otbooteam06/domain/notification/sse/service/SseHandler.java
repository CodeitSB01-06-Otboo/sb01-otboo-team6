package com.codeit.sb01otbooteam06.domain.notification.sse.service;

import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.event.NotificationCreateEvent;
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

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationCreateEvent event) {
    NotificationDto dto = event.notificationDto();
    sseService.send(dto.receiverId(), "notifications", dto);
  }
}
