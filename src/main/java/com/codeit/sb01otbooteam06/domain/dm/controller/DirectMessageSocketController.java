package com.codeit.sb01otbooteam06.domain.dm.controller;

import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageCreateRequest;
import com.codeit.sb01otbooteam06.domain.dm.service.DirectMessageService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class DirectMessageSocketController {

    private final DirectMessageService dmService;

    /* STOMP 메시지 수신 */
    @MessageMapping("direct-messages_send")
    public void handleSend(DirectMessageCreateRequest req) {

        log.info("[WS-IN ] {} → {} \"{}\"",
            req.senderId(), req.receiverId(),
            preview(req.content()));

        dmService.send(req.senderId(), req.receiverId(), req.content());
    }

    /* 짧은 본문 미리보기 */
    private String preview(String txt) {
        return txt.length() > 40 ? txt.substring(0, 40) + "…" : txt;
    }
}
