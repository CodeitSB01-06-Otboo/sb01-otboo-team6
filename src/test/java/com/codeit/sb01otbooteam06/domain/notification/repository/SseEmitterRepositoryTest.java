package com.codeit.sb01otbooteam06.domain.notification.repository;

import com.codeit.sb01otbooteam06.domain.notification.sse.repository.SseEmitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterRepositoryTest {

  private SseEmitterRepository repository;

  @BeforeEach
  void setUp() {
    repository = new SseEmitterRepository();
  }

  @Test
  @DisplayName("SseEmitter 저장 및 조회")
  void saveAndFindByReceiverId() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter = new SseEmitter();

    repository.save(receiverId, emitter);

    Optional<List<SseEmitter>> result = repository.findByReceiverId(receiverId);
    assertThat(result).isPresent();
    assertThat(result.get()).contains(emitter);
  }

  @Test
  @DisplayName("여러 사용자에게 emitter를 저장하고 전체 조회")
  void findAllByReceiverIdsIn() {
    UUID receiver1 = UUID.randomUUID();
    UUID receiver2 = UUID.randomUUID();

    SseEmitter emitter1 = new SseEmitter();
    SseEmitter emitter2 = new SseEmitter();
    SseEmitter emitter3 = new SseEmitter();

    repository.save(receiver1, emitter1);
    repository.save(receiver1, emitter2);
    repository.save(receiver2, emitter3);

    List<SseEmitter> result = repository.findAllByReceiverIdsIn(List.of(receiver1, receiver2));

    assertThat(result).containsExactlyInAnyOrder(emitter1, emitter2, emitter3);
  }

  @Test
  @DisplayName("전체 emitter를 조회할 수 있다.")
  void findAll() {
    UUID receiver1 = UUID.randomUUID();
    UUID receiver2 = UUID.randomUUID();

    SseEmitter emitter1 = new SseEmitter();
    SseEmitter emitter2 = new SseEmitter();

    repository.save(receiver1, emitter1);
    repository.save(receiver2, emitter2);

    List<SseEmitter> allEmitters = repository.findAll();

    assertThat(allEmitters).containsExactlyInAnyOrder(emitter1, emitter2);
  }


  @Test
  @DisplayName("SseEmitter 삭제")
  void deleteEmitter() {
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter1 = new SseEmitter();
    SseEmitter emitter2 = new SseEmitter();

    repository.save(receiverId, emitter1);
    repository.save(receiverId, emitter2);

    repository.delete(receiverId, emitter1);

    Optional<List<SseEmitter>> result = repository.findByReceiverId(receiverId);
    assertThat(result).isPresent();
    assertThat(result.get()).containsExactly(emitter2);
  }

  @Test
  @DisplayName("없는 사용자 ID로 조회 시 Optional.empty() 반환")
  void findByUnknownReceiverId() {
    Optional<List<SseEmitter>> result = repository.findByReceiverId(UUID.randomUUID());
    assertThat(result).isEmpty();
  }
}
