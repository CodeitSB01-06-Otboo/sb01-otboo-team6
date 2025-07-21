package com.codeit.sb01otbooteam06.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.codeit.sb01otbooteam06.domain.follow.dto.FollowDto;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowListResponse;
import com.codeit.sb01otbooteam06.domain.follow.dto.FollowSummaryDto;
import com.codeit.sb01otbooteam06.domain.follow.entity.Follow;
import com.codeit.sb01otbooteam06.domain.follow.repository.FollowRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    FollowRepository followRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    FollowService followService;

    UUID followerId;
    UUID followeeId;
    User follower;
    User followee;

    @BeforeEach
    void setUp() {
        followerId = UUID.randomUUID();
        followeeId = UUID.randomUUID();

        follower = createUser(followerId, "팔로워");
        followee = createUser(followeeId, "팔로이");
    }

    @Test
    @DisplayName("다른 사용자를 팔로우할 수 있다")
    void canFollowOtherUser() {
        // given
        given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
        given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
        given(followRepository.existsByFollowerAndFollowee(follower, followee)).willReturn(false);
        given(followRepository.save(any(Follow.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        FollowDto dto = followService.follow(followerId, followeeId);

        // then
        then(followRepository).should().save(any(Follow.class));
        assertThat(dto.follower().getUserId()).isEqualTo(followerId);
        assertThat(dto.followee().getUserId()).isEqualTo(followeeId);
    }

    @Test
    @DisplayName("이미 팔로우한 사용자에게 또 팔로우 요청하면 예외가 발생한다")
    void duplicateFollowThrows() {
        // given
        given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
        given(userRepository.findById(followeeId)).willReturn(Optional.of(followee));
        given(followRepository.existsByFollowerAndFollowee(follower, followee)).willReturn(true);

        // expect
        assertThatThrownBy(() -> followService.follow(followerId, followeeId))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("언팔로우 시 팔로우 정보가 없으면 예외가 발생한다")
    void unfollowUnknownFollowThrows() {
        // given
        UUID followId = UUID.randomUUID();
        given(followRepository.findById(followId)).willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> followService.unfollowById(followerId, followId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("팔로워 목록을 조회할 수 있다")
    void listFollowers() {
        // given
        Follow follow = Follow.from(follower, followee);
        ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());

        given(followRepository.findFollowers(
            eq(followeeId),
            isNull(),
            any(Pageable.class)))
            .willReturn(List.of(follow));

        // when
        FollowListResponse res = followService.followers(followeeId, null, 20);

        // then
        assertThat(res.data()).hasSize(1);
        assertThat(res.hasNext()).isFalse();
    }

    @Test
    @DisplayName("내 팔로우를 정상적으로 취소할 수 있다")
    void unfollowSuccess() {
        Follow follow = Follow.from(follower, followee);
        ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());

        given(followRepository.findById(follow.getId()))
            .willReturn(Optional.of(follow));

        followService.unfollowById(followerId, follow.getId());

        then(followRepository).should().delete(follow);
    }

    @Test
    @DisplayName("팔로우 요약 정보를 가져온다")
    void summary() {
        given(followRepository.countByFolloweeId(followeeId)).willReturn(3L); // 팔로워 3명
        given(followRepository.countByFollowerId(followeeId)).willReturn(2L); // 팔로잉 2명
        given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
            .willReturn(Optional.empty());
        given(followRepository.existsByFollowerIdAndFolloweeId(followeeId, followerId))
            .willReturn(false);

        FollowSummaryDto dto = followService.getSummary(followerId, followeeId);

        assertThat(dto.followerCount()).isEqualTo(3);
        assertThat(dto.followingCount()).isEqualTo(2);
        assertThat(dto.followedByMe()).isFalse();
        assertThat(dto.followingMe()).isFalse();
    }

    @Test
    @DisplayName("다른 사용자가 내 팔로우를 취소하려 하면 AccessDeniedException")
    void unfollowByOthersThrows() {
        // given: followee가 follower를 팔로우한 관계
        Follow follow = Follow.from(follower, followee);
        ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());

        given(followRepository.findById(follow.getId())).willReturn(Optional.of(follow));

        // 내가 아닌 제3자 ID
        UUID otherUser = UUID.randomUUID();

        // expect
        assertThatThrownBy(() -> followService.unfollowById(otherUser, follow.getId()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("팔로워 목록에 다음 페이지가 있으면 hasNext=true, nextCursor 채워진다")
    void listFollowers_hasNext() {
        // given
        Follow follow = Follow.from(follower, followee);
        ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());

        int size = 1;                              // 요청 페이지 크기 = 1
        List<Follow> raw = List.of(follow);        // content 역시 1개
        Pageable page = PageRequest.ofSize(size);

        given(followRepository.findFollowers(
            eq(followeeId),
            isNull(),
            any(Pageable.class)))
            .willReturn(raw);

        // when
        FollowListResponse res = followService.followers(followeeId, null, size);

        // then
        assertThat(res.hasNext()).isTrue();                 // ← slice.hasNext() == true 분기
        assertThat(res.nextCursor()).isEqualTo(follow.getId());
    }

    @Test
    @DisplayName("팔로잉 목록에 다음 페이지가 있으면 hasNext=true, nextCursor 채워진다")
    void listFollowings_hasNext() {
        // given
        Follow follow = Follow.from(follower, followee);       // follower → followee
        ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());

        int size = 1;                                          // 요청 페이지 크기 = 1
        List<Follow> raw = List.of(follow);                    // content 역시 1건

        given(followRepository.findFollowings(
            eq(followerId),
            isNull(),
            any(Pageable.class)))
            .willReturn(raw);

        // when
        FollowListResponse res = followService.followings(followerId, null, size);

        // then
        assertThat(res.hasNext()).isTrue();                    // 삼항 연산자 true 분기
        assertThat(res.nextCursor()).isEqualTo(follow.getId());
    }

    // 실제 도메인 객체 생성
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
