package com.codeit.sb01otbooteam06.domain.follow.service;

import com.codeit.sb01otbooteam06.domain.follow.dto.FollowDto;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowListResponse;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowSummaryDto;
import com.codeit.sb01otbooteam06.domain.follow.entity.Follow;
import com.codeit.sb01otbooteam06.domain.follow.repository.FollowRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowDto follow(UUID followerId, UUID followeeId) {
        User follower = getUser(followerId);
        User followee = getUser(followeeId);
        log.info("[FOLLOW] {} → {}", followerId, followeeId);
        if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
            throw new IllegalStateException("이미 팔로우 중입니다.");
        }
        Follow follow = followRepository.save(Follow.from(follower, followee));
        return FollowDto.from(follow);
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followeeId) {
        User follower = getUser(followerId);
        User followee = getUser(followeeId);
        log.info("[UNFOLLOW] {} → {}", followerId, followeeId);
        followRepository.deleteByFollowerAndFollowee(follower, followee);
    }

    public FollowSummaryDto getSummary(UUID me, UUID target) {

        long followerCnt  = followRepository.countByFolloweeId(target);
        long followingCnt = followRepository.countByFollowerId(target);
        log.debug("[SUMMARY] requester={}, target={}", me, target);
        boolean followedByMe = false;
        UUID    followedByMeId = null;
        boolean followingMe  = false;

        if (me != null) {   // 로그인 사용자 있을 때만 추가 정보 계산
            Optional<Follow> myFollow = followRepository.findByFollowerIdAndFolloweeId(me, target);
            followedByMe   = myFollow.isPresent();
            followedByMeId = myFollow.map(Follow::getId).orElse(null);

            followingMe = followRepository.existsByFollowerIdAndFolloweeId(target, me);
        }

        return new FollowSummaryDto(
            target, followerCnt, followingCnt,
            followedByMe, followedByMeId, followingMe);
    }

    /* 팔로워 목록 */
    public FollowListResponse followers(UUID followeeId, UUID cursor, int size) {
        Pageable page = PageRequest.ofSize(size);
        log.debug("[SVC] followers followeeId={}, cursor={}, size={}", followeeId, cursor, size);

        Slice<FollowDto> slice = new SliceImpl<>(
            followRepository.findFollowers(followeeId, cursor, page)
                .stream()
                .map(FollowDto::from)
                .toList(),
            page,
            false);          // hasNext 는 아래에서 계산

        slice = slice.hasContent() && slice.getContent().size() == size
            ? new SliceImpl<>(slice.getContent(), page, true)
            : slice;

        return FollowListResponse.fromSlice(slice, "createdAt", "DESCENDING");
    }

    /* 팔로잉 목록 */
    public FollowListResponse followings(UUID followerId, UUID cursor, int size) {
        Pageable page = PageRequest.ofSize(size);
        log.debug("[SVC] followings followerId={}, cursor={}, size={}", followerId, cursor, size);

        Slice<FollowDto> slice = new SliceImpl<>(
            followRepository.findFollowings(followerId, cursor, page)
                .stream()
                .map(FollowDto::from)
                .toList(),
            page,
            false);

        slice = slice.hasContent() && slice.getContent().size() == size
            ? new SliceImpl<>(slice.getContent(), page, true)
            : slice;

        return FollowListResponse.fromSlice(slice, "createdAt", "DESCENDING");
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
