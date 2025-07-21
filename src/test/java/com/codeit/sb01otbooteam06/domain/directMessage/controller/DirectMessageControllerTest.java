package com.codeit.sb01otbooteam06.domain.directMessage.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.sb01otbooteam06.domain.dm.controller.DirectMessageController;
import com.codeit.sb01otbooteam06.domain.dm.dto.DirectMessageListResponse;
import com.codeit.sb01otbooteam06.domain.dm.service.DirectMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DirectMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class DirectMessageControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    DirectMessageService dmService;

    @Autowired
    ObjectMapper objectMapper;   // Spring Boot Test가 자동 주입

    @Test
    @DisplayName("GET /api/direct-messages 는 서비스 list()를 호출하고 200 OK를 리턴한다")
    void getDmList() throws Exception {
        UUID userId = UUID.randomUUID();

        // 서비스 스텁: 빈 리스트 반환
        DirectMessageListResponse dummy =
            new DirectMessageListResponse(List.of(), null, null,
                false, 0, "createdAt", "DESCENDING");
        given(dmService.list(userId, null, 15)).willReturn(dummy);

        mockMvc.perform(get("/api/direct-messages")
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(dummy)));

        then(dmService).should().list(userId, null, 15);
    }
}
