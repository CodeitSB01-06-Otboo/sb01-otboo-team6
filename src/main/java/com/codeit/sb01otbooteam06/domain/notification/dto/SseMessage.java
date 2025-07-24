package com.codeit.sb01otbooteam06.domain.notification.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessage {

  private UUID eventId;
  private Set<UUID> receiverIds = new HashSet<>();
  private boolean broadcast;
  private String eventName;
  private Object eventData;

  public static SseMessage create(UUID receiverId, String eventName, Object eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        Set.of(receiverId),
        false,
        eventName,
        eventData
    );
  }

  public static SseMessage create(Collection<UUID> receiverIds, String eventName,
      Object eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        new HashSet<>(receiverIds),
        false,
        eventName,
        eventData
    );
  }

  public static SseMessage createBroadcast(String eventName, Object eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        new HashSet<>(),
        true,
        eventName,
        eventData
    );
  }

  public boolean isReceivable(UUID receiverId) {
    return broadcast || receiverIds.contains(receiverId);
  }

  public Set<DataWithMediaType> toEvent() {
    return SseEmitter.event()
        .id(eventId.toString())
        .name(eventName)
        .data(eventData)
        .build();
  }

}
