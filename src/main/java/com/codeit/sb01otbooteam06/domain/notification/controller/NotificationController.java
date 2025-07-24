package com.codeit.sb01otbooteam06.domain.notification.controller;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.notification.dto.NotificationDto;
import com.codeit.sb01otbooteam06.domain.notification.service.NotificationService;
import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final AuthService authService;

  @GetMapping
  public ResponseEntity<PageResponse<NotificationDto>> getCursorNotifications(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam(required = true) int limit
  ) {
    UUID userId = authService.getCurrentUserId();

    Instant cursorCreatedAt = null;

    if (cursor != null && !cursor.isBlank()) {
      try {
        cursorCreatedAt = Instant.parse(cursor);
      } catch (DateTimeParseException e) {
        //todo : INVALID_CURSOR_FORMAT 로 만들어서 나중에 변경
        throw new OtbooException(ErrorCode.ILLEGAL_ARGUMENT_ERROR);
      }
    }

    PageResponse<NotificationDto> response = notificationService.getNotificationsByCursor(
        userId,
        cursorCreatedAt,
        idAfter,
        limit,
        "createdAt",
        "DESC"
    );

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
    UUID userId = authService.getCurrentUserId();
    //todo : 알림을 물리 삭제 할지, 논리 삭제 할지 생각해봐야 될듯 -> 현재는 논리 삭제
    notificationService.markAsRead(userId, notificationId);
    return ResponseEntity.noContent().build();
  }

}
