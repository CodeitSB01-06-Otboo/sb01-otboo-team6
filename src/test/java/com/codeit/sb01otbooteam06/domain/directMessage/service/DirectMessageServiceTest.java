package com.codeit.sb01otbooteam06.domain.directMessage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;

import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageDto;
import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageListResponse;
import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import com.codeit.sb01otbooteam06.domain.dm.repository.DirectMessageRepository;
import com.codeit.sb01otbooteam06.domain.dm.service.DirectMessageService;
import com.codeit.sb01otbooteam06.domain.notification.service.NotificationService;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

    @Mock
    DirectMessageRepository dmRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SimpMessagingTemplate messagingTemplate;
    @Mock
    NotificationService notificationService;

    @InjectMocks
    DirectMessageService dmService;

    UUID senderId;
    UUID receiverId;
    User sender;
    User receiver;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        sender = createUser(senderId, "보낸이");
        receiver = createUser(receiverId, "받는이");

    }

    @Test
    @DisplayName("DM을 전송하면 저장되고 브로드캐스팅되며 알림이 발행된다")
    void sendDmSavesBroadcastsAndPublishesNotification() {
        // given
        given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(dmRepository.save(any(DirectMessage.class)))
            .willAnswer(inv -> {
                DirectMessage dm = inv.getArgument(0, DirectMessage.class);
                ReflectionTestUtils.setField(dm, "id", UUID.randomUUID());
                return dm;
            });
        String content = "hello";

        // when
        UUID returnedId = dmService.send(senderId, receiverId, content);

        // then
        then(dmRepository).should().save(any(DirectMessage.class));
        then(messagingTemplate).should().convertAndSend(
            eq("/sub/direct-messages_" + DirectMessage.generateKey(senderId, receiverId)),
            any(DirectMessageDto.class)
        );
        then(notificationService).should().notifyDirectMessage(sender, receiver, content);

        assertThat(returnedId).isNotNull();
    }

    @Test
    @DisplayName("DM 목록을 페이징 조회할 수 있다")
    void listDmRooms() {
        DirectMessage dm = DirectMessage.from(sender, receiver, "hi");
        ReflectionTestUtils.setField(dm, "id", UUID.randomUUID());

        Page<DirectMessage> page =
            new PageImpl<>(List.of(dm), PageRequest.of(0, 15), 1);

        given(dmRepository.findLatestPerRoom(
            eq(senderId),
            isNull(),
            any(Pageable.class)))
            .willReturn(page);

        DirectMessageListResponse res = dmService.list(senderId, null, 15);

        assertThat(res.data()).hasSize(1);
        assertThat(res.hasNext()).isFalse();
        assertThat(res.nextCursor()).isNull();
    }

    @Test
    @DisplayName("DM 목록이 다음 페이지를 가리키면 nextCursor가 채워진다")
    void listDmRooms_hasNext() {
        DirectMessage dm1 = DirectMessage.from(sender, receiver, "first");
        ReflectionTestUtils.setField(dm1, "id", UUID.randomUUID());

        int size = 1;   // page size
        Page<DirectMessage> page =
            new PageImpl<>(List.of(dm1), PageRequest.of(0, size), 2); // totalElements=2

        given(dmRepository.findLatestPerRoom(eq(senderId), isNull(), any(Pageable.class)))
            .willReturn(page);

        DirectMessageListResponse res = dmService.list(senderId, null, size);

        assertThat(res.hasNext()).isTrue();
        assertThat(res.nextCursor()).isEqualTo(dm1.getId());
    }

    private User createUser(UUID id, String name) {
        Profile profile = new Profile(
            null, name, Gender.MALE,
            LocalDate.of(1990, 1, 1),
            37.0, 127.0, 60, 120,
            List.of("서울"), 0, null);

        User user = User.builder()
            .email(name + "@example.com")
            .password("pw")
            .name(name)
            .role(Role.USER)
            .linkedOAuthProviders(List.of())
            .build();

        user.setProfile(profile);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(profile, "id", id);
        ReflectionTestUtils.setField(profile, "user", user);
        return user;
    }
}
