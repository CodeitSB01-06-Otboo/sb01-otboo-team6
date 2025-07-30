package com.codeit.sb01otbooteam06.domain.notification.repository;

import com.codeit.sb01otbooteam06.domain.notification.sse.dto.SseMessage;
import com.codeit.sb01otbooteam06.domain.notification.sse.repository.SseMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseMessageRepositoryTest {

  private SseMessageRepository repository;
  private UUID receiverId;

  @BeforeEach
  void setUp() {
    repository = new SseMessageRepository();
    receiverId = UUID.randomUUID();
    ReflectionTestUtils.setField(repository, "eventQueueCapacity", 3);
  }

  @Test
  @DisplayName("단일 수신자 메시지를 저장하면 반환되고 내부에도 저장된다")
  void saveMessage_singleReceiver() {
    SseMessage message = SseMessage.create(receiverId, "test-event", "data");

    SseMessage saved = repository.save(message);

    assertThat(saved).isEqualTo(message);
    List<SseMessage> result = repository.findAllByEventIdAfterAndReceiverId(message.getEventId(), receiverId);
    assertThat(result).isEmpty(); // 기준 eventId 이후가 없기 때문에 비어 있음
  }

  @Test
  @DisplayName("여러 메시지 중 지정된 ID 이후의 메시지만 필터링하여 반환한다")
  void findAllByEventIdAfterAndReceiverId_filtersProperly() {
    SseMessage msg1 = SseMessage.create(receiverId, "event1", "1");
    SseMessage msg2 = SseMessage.create(receiverId, "event2", "2");
    SseMessage msg3 = SseMessage.create(UUID.randomUUID(), "event3", "3"); // 다른 사용자

    repository.save(msg1);
    repository.save(msg2);
    repository.save(msg3);

    List<SseMessage> result = repository.findAllByEventIdAfterAndReceiverId(msg1.getEventId(), receiverId);

    assertThat(result).containsExactly(msg2); // msg3은 수신자 다름
  }

  @Test
  @DisplayName("수신자가 여러 명인 경우도 isReceivable 정상 동작")
  void findAll_withMultipleReceivers() {
    UUID receiverA = UUID.randomUUID();
    UUID receiverB = UUID.randomUUID();

    SseMessage message = SseMessage.create(List.of(receiverA, receiverB), "event", "data");

    repository.save(message);

    List<SseMessage> resultA = repository.findAllByEventIdAfterAndReceiverId(message.getEventId(), receiverA);
    List<SseMessage> resultB = repository.findAllByEventIdAfterAndReceiverId(message.getEventId(), receiverB);

    assertThat(resultA).isEmpty(); // 기준 eventId 이후 없음
    assertThat(resultB).isEmpty(); // 기준 eventId 이후 없음
  }

  @Test
  @DisplayName("브로드캐스트 메시지는 모든 사용자에게 수신 가능")
  void broadcastMessage_isReceivableByAnyReceiver() {
    UUID otherReceiver = UUID.randomUUID();
    SseMessage broadcastMessage = SseMessage.createBroadcast("broadcast", "공지");

    repository.save(broadcastMessage);

    List<SseMessage> result = repository.findAllByEventIdAfterAndReceiverId(broadcastMessage.getEventId(), otherReceiver);

    assertThat(result).isEmpty(); // 기준 eventId 이후 메시지 없음

    // 새로운 broadcast 메시지 이후 테스트
    SseMessage newerBroadcast = SseMessage.createBroadcast("broadcast-2", "공지2");
    repository.save(newerBroadcast);

    List<SseMessage> result2 = repository.findAllByEventIdAfterAndReceiverId(broadcastMessage.getEventId(), otherReceiver);
    assertThat(result2).containsExactly(newerBroadcast);
  }

  @Test
  @DisplayName("용량 초과 시 가장 오래된 메시지는 삭제된다")
  void capacityExceeded_evictsOldest() {
    // given
    SseMessage msg1 = SseMessage.create(receiverId, "event1", "1");
    SseMessage msg2 = SseMessage.create(receiverId, "event2", "2");
    SseMessage msg3 = SseMessage.create(receiverId, "event3", "3");

    repository.save(msg1); // 용량 1/3
    repository.save(msg2); // 용량 2/3
    repository.save(msg3); // 용량 3/3

    // when
    SseMessage msg4 = SseMessage.create(receiverId, "event4", "4"); // msg1은 제거됨
    repository.save(msg4); // 용량 유지: msg2, msg3, msg4

    // then
    // msg2는 남아있음 → 기준으로 이후 메시지(msg3, msg4) 조회
    List<SseMessage> result = repository.findAllByEventIdAfterAndReceiverId(msg2.getEventId(), receiverId);

    // → "event3", "event4"만 남아 있어야 함
    assertThat(result)
        .extracting(SseMessage::getEventName)
        .containsExactly("event3", "event4");
  }
}