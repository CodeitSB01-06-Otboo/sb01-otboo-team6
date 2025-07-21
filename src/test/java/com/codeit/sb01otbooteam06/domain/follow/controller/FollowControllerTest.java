package com.codeit.sb01otbooteam06.domain.follow.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowCreateRequest;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowDto;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowListResponse;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowSummaryDto;
import com.codeit.sb01otbooteam06.domain.follow.service.FollowService;
import com.codeit.sb01otbooteam06.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FollowControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FollowService followService;

    @MockitoBean
    AuthService authService;

    /* ---------- 1. 정상: 헤더 포함 ---------- */
    @Test
    @DisplayName("POST /api/follows : 헤더의 X-USER-ID 를 그대로 사용해 followService.follow 호출")
    void follow_withHeader() throws Exception {
        UUID me       = UUID.randomUUID();
        UUID followee = UUID.randomUUID();

        FollowCreateRequest req = new FollowCreateRequest(me, followee);

        given(followService.follow(me, followee))
            .willReturn(mock(FollowDto.class));

        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", me.toString())
                .content(objectMapper.writeValueAsBytes(req)))
            .andExpect(status().isOk());

        then(followService).should().follow(me, followee);
        then(authService).shouldHaveNoInteractions();
    }

    /* ---------- 2. 정상: 헤더 없이 JWT 사용 ---------- */
    @Test
    @DisplayName("POST /api/follows : 헤더 없으면 AuthService로 현재 사용자 ID 조회 후 follow")
    void follow_withoutHeader() throws Exception {
        UUID me       = UUID.randomUUID();
        UUID followee = UUID.randomUUID();

        FollowCreateRequest req = new FollowCreateRequest(null, followee);

        given(authService.getCurrentUserId()).willReturn(me);
        given(followService.follow(me, followee)).willReturn(mock(FollowDto.class));

        mockMvc.perform(post("/api/follows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(req)))
            .andExpect(status().isOk());

        then(authService).should().getCurrentUserId();
        then(followService).should().follow(me, followee);
    }

    /* ---------- 3. 예외: body·header 불일치 ---------- */
    @Test
    @DisplayName("POST /api/follows : body.followerId 와 헤더가 다르면 400")
    void follow_mismatchedIds() throws Exception {
        UUID headerId = UUID.randomUUID();
        UUID bodyId   = UUID.randomUUID();      // 다른 값
        UUID followee = UUID.randomUUID();

        FollowCreateRequest req = new FollowCreateRequest(bodyId, followee);

        mockMvc.perform(post("/api/follows")
                .header("X-USER-ID", headerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(req)))
            .andExpect(status().isBadRequest())                 // 400
            .andExpect(jsonPath("$.code").value("FOLLOWER_MISMATCH"))  // 선택
            .andExpect(jsonPath("$.message")
                .value("followerId가 인증 정보와 일치하지 않습니다."));

        then(followService).shouldHaveNoInteractions();
    }

    /* ---------- 4. DELETE /api/follows/{id} : 헤더 포함 ---------- */
    @Test
    @DisplayName("DELETE /api/follows/{id} : 헤더의 X-USER-ID 로 언팔로우")
    void unfollow_withHeader() throws Exception {
        UUID me = UUID.randomUUID();
        UUID followId = UUID.randomUUID();

        mockMvc.perform(delete("/api/follows/{id}", followId)
                .header("X-USER-ID", me.toString()))
            .andExpect(status().isOk());

        then(followService).should().unfollowById(me, followId);
        then(authService).shouldHaveNoInteractions();
    }

    /* ---------- 5. DELETE /api/follows/{id} : 헤더 없으면 AuthService ---------- */
    @Test
    @DisplayName("DELETE /api/follows/{id} : 헤더 없으면 JWT ID 조회 후 언팔로우")
    void unfollow_withoutHeader() throws Exception {
        UUID me = UUID.randomUUID();
        UUID followId = UUID.randomUUID();

        given(authService.getCurrentUserId()).willReturn(me);

        mockMvc.perform(delete("/api/follows/{id}", followId))
            .andExpect(status().isOk());

        then(authService).should().getCurrentUserId();
        then(followService).should().unfollowById(me, followId);
    }

    /* ---------- 6. GET /api/follows/summary ---------- */
    @Test
    @DisplayName("GET /api/follows/summary : 요약 정보를 반환한다")
    void getSummary() throws Exception {
        UUID me = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        FollowSummaryDto dto = new FollowSummaryDto(
            target, 3, 2, false, null, false);

        given(authService.getCurrentUserId()).willReturn(me);
        given(followService.getSummary(me, target)).willReturn(dto);

        mockMvc.perform(get("/api/follows/summary")
                .param("userId", target.toString()))
            .andExpect(status().isOk());

        then(followService).should().getSummary(me, target);
    }

    /* ---------- 7. GET /api/follows/followers ---------- */
    @Test
    @DisplayName("GET /api/follows/followers : 팔로워 목록 조회")
    void listFollowers() throws Exception {
        UUID followee = UUID.randomUUID();

        FollowListResponse res =
            new FollowListResponse(List.of(), null, null, false, 0,
                "createdAt", "DESCENDING");

        given(followService.followers(followee, null, 20)).willReturn(res);

        mockMvc.perform(get("/api/follows/followers")
                .param("followeeId", followee.toString()))
            .andExpect(status().isOk());

        then(followService).should().followers(followee, null, 20);
    }

    /* ---------- 8. GET /api/follows/followings ---------- */
    @Test
    @DisplayName("GET /api/follows/followings : 팔로잉 목록 조회")
    void listFollowings() throws Exception {
        UUID follower = UUID.randomUUID();

        FollowListResponse res =
            new FollowListResponse(List.of(), null, null, false, 0,
                "createdAt", "DESCENDING");

        given(followService.followings(follower, null, 20)).willReturn(res);

        mockMvc.perform(get("/api/follows/followings")
                .param("followerId", follower.toString()))
            .andExpect(status().isOk());

        then(followService).should().followings(follower, null, 20);
    }
}