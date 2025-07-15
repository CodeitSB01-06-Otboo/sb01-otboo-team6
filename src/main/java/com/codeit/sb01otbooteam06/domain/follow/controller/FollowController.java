package com.codeit.sb01otbooteam06.domain.follow.controller;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowCreateRequest;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowDto;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowListResponse;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowSummaryDto;
import com.codeit.sb01otbooteam06.domain.follow.service.FollowService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final FollowService followService;
    private final AuthService    authService;          // ★ 있으면 활용, 없으면 X-USER-ID 유지

    /** 팔로우 */
    @PostMapping
    public FollowDto follow(@RequestBody FollowCreateRequest req,
        @RequestHeader(value = "X-USER-ID", required = false) UUID me) {

        /* ① 헤더가 없으면 JWT → AuthService */
        if (me == null) {
            me = authService.getCurrentUserId();     // throws if not logged-in
        }

        /* ② 바디(followerId)와 헤더/JWT 값 불일치 → 400 */
        if (req.followerId() != null && !req.followerId().equals(me)) {
            throw new IllegalArgumentException("followerId가 인증 정보와 일치하지 않습니다.");
        }

        log.info("[HTTP] POST /follows  me={}, followee={}", me, req.followeeId());
        return followService.follow(me, req.followeeId());
    }

    /** 언팔로우 */
    @DeleteMapping("/{followeeId}")
    public void unfollow(@PathVariable UUID followeeId,
        @RequestHeader(value = "X-USER-ID", required = false) UUID me) {
        followService.unfollow(me, followeeId);
    }

    /** 특정 사용자의 팔로우/팔로잉 요약 */
    @GetMapping("/summary")
    public FollowSummaryDto summary(@RequestParam UUID userId,
        @RequestHeader(value = "X-USER-ID", required = false) UUID me) {
        log.info("[HTTP] GET /summary  userId={}, me={}", userId, me);
        return followService.getSummary(me, userId);
    }
    /* ----------------------------- */

    /** 팔로워 목록 (followeeId 기준) */
    @GetMapping("/followers")
    public FollowListResponse followers(@RequestParam UUID followeeId,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") int limit) {
        log.info("[HTTP] GET /followers followeeId={}, cursor={}, limit={}",
            followeeId, cursor, limit);
        return followService.followers(followeeId, cursor, limit);
    }

    /** 팔로잉 목록 (followerId 기준) */
    @GetMapping("/followings")
    public FollowListResponse followings(@RequestParam UUID followerId,
        @RequestParam(required = false) UUID cursor,
        @RequestParam(defaultValue = "20") int limit) {
        log.info("[HTTP] GET /followings followerId={}, cursor={}, limit={}",
            followerId, cursor, limit);
        return followService.followings(followerId, cursor, limit);
    }
}
