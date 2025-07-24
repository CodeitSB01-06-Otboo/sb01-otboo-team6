package com.codeit.sb01otbooteam06.domain.notification.service;

import com.codeit.sb01otbooteam06.domain.notification.dto.SseMessage;
import com.codeit.sb01otbooteam06.domain.notification.repository.SseEmitterRepository;
import com.codeit.sb01otbooteam06.domain.notification.repository.SseMessageRepository;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  @Value("${sse.timeout}")
  private long timeout;

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  public SseEmitter connect(UUID receiverId, @RequestHeader(value = "Last-Event-ID", required = false) UUID lastEventId) {
    SseEmitter sseEmitter = new SseEmitter(timeout);

    log.info("SSE 연결 요청 - receiverId: {}, Last-Event-ID: {}", receiverId, lastEventId);


    sseEmitter.onCompletion(() -> {
      log.debug("sse on onCompletion");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });
    sseEmitter.onTimeout(() -> {
      log.debug("sse on onTimeout");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });
    sseEmitter.onError((ex) -> {
      log.debug("sse on onError");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });

    sseEmitterRepository.save(receiverId, sseEmitter);

    try {
      sseEmitter.send(SseEmitter.event()
          .id(UUID.randomUUID().toString())
          .name("connect")
          .data("connected")
          .reconnectTime(3000L)
      );
    } catch (IOException e) {
      log.error("초기 연결 메시지 전송 실패", e);
    }

    Optional.ofNullable(lastEventId)
        .ifPresent(id -> {
          sseMessageRepository.findAllByEventIdAfterAndReceiverId(id, receiverId)
              .forEach(sseMessage -> {
                try {
                  sseEmitter.send(sseMessage.toEvent());
                } catch (IOException e) {
                  log.error(e.getMessage(), e);
                }
              });
        });

    return sseEmitter;
  }

  public void send(UUID receiverId, String eventName, Object data) {
    sseEmitterRepository.findByReceiverId(receiverId)
        .ifPresent(sseEmitters -> {
          SseMessage message = sseMessageRepository.save(
              SseMessage.create(receiverId, eventName, data));
          sseEmitters.forEach(sseEmitter -> {
            try {
              sseEmitter.send(message.toEvent());
            } catch (IOException e) {
              log.error(e.getMessage(), e);
              sseEmitter.completeWithError(e);
              sseEmitterRepository.delete(receiverId, sseEmitter);
            }
          });
        });
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    SseMessage message = sseMessageRepository.save(SseMessage.create(receiverIds, eventName, data));
    Set<DataWithMediaType> event = message.toEvent();
    sseEmitterRepository.findAllByReceiverIdsIn(receiverIds)
        .forEach(sseEmitter -> {
          try {
            sseEmitter.send(event);
          } catch (IOException e) {
            log.error(e.getMessage(), e);
          }
        });
  }

  public void broadcast(String eventName, Object data) {
    SseMessage message = sseMessageRepository.save(SseMessage.createBroadcast(eventName, data));
    Set<DataWithMediaType> event = message.toEvent();
    sseEmitterRepository.findAll()
        .forEach(sseEmitter -> {
          try {
            sseEmitter.send(event);
          } catch (IOException e) {
            log.error(e.getMessage(), e);
            sseEmitter.completeWithError(e);

          }
        });
  }

  public void send(SseMessage sseMessage) {
    sseMessageRepository.save(sseMessage);
    Set<DataWithMediaType> event = sseMessage.toEvent();
    if (sseMessage.isBroadcast()) {
      sseEmitterRepository.findAll()
          .forEach(sseEmitter -> {
            try {
              sseEmitter.send(event);
            } catch (IOException e) {
              log.error(e.getMessage(), e);
            }
          });
    } else {
      sseEmitterRepository.findAllByReceiverIdsIn(sseMessage.getReceiverIds())
          .forEach(sseEmitter -> {
            try {
              sseEmitter.send(event);
            } catch (IOException e) {
              log.error(e.getMessage(), e);
            }
          });
    }
  }

  @Scheduled(cron = "0 */30 * * * *")
  public void cleanUp() {
    Set<DataWithMediaType> ping = SseEmitter.event()
        .name("ping")
        .build();
    sseEmitterRepository.findAll()
        .forEach(sseEmitter -> {
          try {
            sseEmitter.send(ping);
          } catch (IOException e) {
            log.error(e.getMessage(), e);
            sseEmitter.completeWithError(e);
          }
        });
  }
}
