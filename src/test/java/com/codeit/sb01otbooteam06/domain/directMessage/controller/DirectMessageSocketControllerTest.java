package com.codeit.sb01otbooteam06.domain.directMessage.controller;

import static org.mockito.BDDMockito.then;

import com.codeit.sb01otbooteam06.domain.dm.controller.DirectMessageSocketController;
import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.sb01otbooteam06.domain.dm.service.DirectMessageService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectMessageSocketControllerTest {

    @Mock
    DirectMessageService dmService;
    @InjectMocks
    DirectMessageSocketController controller;

    @Test
    @DisplayName("STOMP 메시지를 수신하면 dmService.send()를 호출한다")
    void handleSend() {
        // given
        UUID senderId   = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        String content  = "DM";

        DirectMessageCreateRequest req = new DirectMessageCreateRequest(senderId, receiverId, content);

        // when
        controller.handleSend(req);

        // then
        then(dmService).should().send(senderId, receiverId, content);
    }
}
