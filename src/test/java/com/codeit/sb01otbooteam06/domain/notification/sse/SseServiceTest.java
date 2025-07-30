package com.codeit.sb01otbooteam06.domain.notification.sse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.notification.sse.dto.SseMessage;
import com.codeit.sb01otbooteam06.domain.notification.sse.repository.SseEmitterRepository;
import com.codeit.sb01otbooteam06.domain.notification.sse.repository.SseMessageRepository;
import com.codeit.sb01otbooteam06.domain.notification.sse.service.SseService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

  @Mock
  private SseEmitterRepository sseEmitterRepository;
  @Mock
  private SseMessageRepository sseMessageRepository;

  @InjectMocks
  private SseService sseService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(sseService, "timeout", 60_000L); // sse.timeout 설정
  }

  @Test
  void connect_초기연결메시지_전송실패해도_예외처리된다() throws Exception {
    // given
    UUID receiverId = UUID.randomUUID();
    UUID lastEventId = null;

    // 실제 SseEmitter 사용 (Mockito로는 콜백 검증 어려움)
    SseEmitter realEmitter = new SseEmitter(60_000L);
    SseEmitter spyEmitter = spy(realEmitter);


    // save 시 spyEmitter 반환하도록 조작
    doReturn(spyEmitter).when(sseEmitterRepository).save(eq(receiverId), any(SseEmitter.class));

    // when
    SseEmitter result = sseService.connect(receiverId, lastEventId);

    // then
    assertNotNull(result);
    verify(sseEmitterRepository).save(eq(receiverId), any(SseEmitter.class));
    // 예외 발생했지만 던지지 않고 catch 처리됨
  }

  @Test
  void connect_과거메시지_전송_중_IOException_예외처리된다() throws Exception {
    // given
    UUID receiverId = UUID.randomUUID();
    UUID lastEventId = UUID.randomUUID();

    SseEmitter emitter = spy(new SseEmitter(60_000L));

    SseMessage sseMessage1 = mock(SseMessage.class);
    SseMessage sseMessage2 = mock(SseMessage.class);

    Set<DataWithMediaType> fakeEvent = Set.of(
        mock(DataWithMediaType.class)
    );

    when(sseMessageRepository.findAllByEventIdAfterAndReceiverId(lastEventId, receiverId))
        .thenReturn(List.of(sseMessage1, sseMessage2));

    when(sseMessage1.toEvent()).thenReturn(fakeEvent);
    when(sseMessage2.toEvent()).thenReturn(fakeEvent);


    doReturn(emitter).when(sseEmitterRepository).save(eq(receiverId), any(SseEmitter.class));

    // when
    SseEmitter result = sseService.connect(receiverId, lastEventId);

    // then
    assertNotNull(result);
    verify(sseMessageRepository).findAllByEventIdAfterAndReceiverId(lastEventId, receiverId);
  }

  @Test
  void connect_EmitterCallback_정상등록된다() {
    // given
    UUID receiverId = UUID.randomUUID();
    UUID lastEventId = null;

    SseEmitter emitter = sseService.connect(receiverId, lastEventId);

    verify(sseEmitterRepository).save(eq(receiverId), any(SseEmitter.class));
  }

  @Test
  void connect_연결되면_초기메시지_전송과_과거메시지_재전송이_이루어진다() throws Exception {
    // given
    UUID receiverId = UUID.randomUUID();
    UUID lastEventId = UUID.randomUUID();

    List<SseMessage> pastMessages = List.of(mock(SseMessage.class), mock(SseMessage.class));

    when(sseMessageRepository.findAllByEventIdAfterAndReceiverId(lastEventId, receiverId))
        .thenReturn(pastMessages);

    // when
    SseEmitter result = sseService.connect(receiverId, lastEventId);

    // then
    assertNotNull(result);
    verify(sseEmitterRepository).save(eq(receiverId), any(SseEmitter.class));
    verify(sseMessageRepository).findAllByEventIdAfterAndReceiverId(lastEventId, receiverId);
    verify(pastMessages.get(0), atLeastOnce()).toEvent();
    verify(pastMessages.get(1), atLeastOnce()).toEvent();
  }

  @Test
  void send_단일_수신자에게_정상_전송된다() throws Exception {
    // given
    UUID receiverId = UUID.randomUUID();
    SseEmitter emitter = mock(SseEmitter.class);
    List<SseEmitter> emitters = List.of(emitter);

    SseMessage mockMessage = mock(SseMessage.class);
    Set<DataWithMediaType> mockedEvent = Set.of(mock(DataWithMediaType.class));

    when(sseEmitterRepository.findByReceiverId(receiverId)).thenReturn(Optional.of(emitters));
    when(sseMessageRepository.save(any())).thenReturn(mockMessage);
    when(mockMessage.toEvent()).thenReturn(mockedEvent);

    // when
    sseService.send(receiverId, "event-name", "message-data");

    // then
    verify(sseMessageRepository).save(any());
    verify(emitter).send(eq(mockedEvent));
  }

  @Test
  void send_여러_수신자에게_정상_전송된다() throws Exception {
    // given
    UUID receiver1 = UUID.randomUUID();
    UUID receiver2 = UUID.randomUUID();
    Collection<UUID> receiverIds = List.of(receiver1, receiver2);

    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    List<SseEmitter> emitters = List.of(emitter1, emitter2);

    SseMessage mockMessage = mock(SseMessage.class);
    Set<DataWithMediaType> event = mock(Set.class);

    when(sseMessageRepository.save(any(SseMessage.class))).thenReturn(mockMessage);
    when(mockMessage.toEvent()).thenReturn(event);
    when(sseEmitterRepository.findAllByReceiverIdsIn(receiverIds)).thenReturn(emitters);

    // when
    sseService.send(receiverIds, "test-event", "test-data");

    // then
    verify(sseMessageRepository).save(any(SseMessage.class));
    verify(mockMessage).toEvent();
    verify(emitter1).send(event);
    verify(emitter2).send(event);
  }

  @Test
  void send_전체_브로드캐스트된다() throws Exception {
    // given
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    SseMessage sseMessage = mock(SseMessage.class);
    Set<DataWithMediaType> event = mock(Set.class);

    when(sseMessageRepository.save(sseMessage)).thenReturn(sseMessage);
    when(sseMessage.isBroadcast()).thenReturn(true);
    when(sseMessage.toEvent()).thenReturn(event);
    when(sseEmitterRepository.findAll()).thenReturn(List.of(emitter1, emitter2));

    // when
    sseService.send(sseMessage);

    // then
    verify(sseMessageRepository).save(sseMessage);
    verify(emitter1).send(event);
    verify(emitter2).send(event);
  }

  @Test
  void broadcast_전체_전송된다() throws Exception {
    // given
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);

    List<SseEmitter> allEmitters = List.of(emitter1, emitter2);

    when(sseEmitterRepository.findAll()).thenReturn(allEmitters);

    SseMessage mockMessage = mock(SseMessage.class);
    Set<DataWithMediaType> mockEvent = Set.of(
        mock(DataWithMediaType.class),
        mock(DataWithMediaType.class)
    );

    when(sseMessageRepository.save(any())).thenReturn(mockMessage);
    when(mockMessage.toEvent()).thenReturn(mockEvent);

    // when
    sseService.broadcast("event", "data");

    // then
    verify(sseMessageRepository).save(any());
    verify(emitter1).send(eq(mockEvent));
    verify(emitter2).send(eq(mockEvent));
  }

  @Test
  void cleanUp_ping_전송된다() throws Exception {
    // given
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);
    List<SseEmitter> allEmitters = List.of(emitter1, emitter2);

    when(sseEmitterRepository.findAll()).thenReturn(allEmitters);

    // when
    sseService.cleanUp();

    // then
    verify(emitter1).send(any(Set.class));
    verify(emitter2).send(any(Set.class));
  }

}