package com.codeit.sb01otbooteam06.domain.feed.like.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.entity.FeedLike;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedLikeRepository;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedRepository;
import com.codeit.sb01otbooteam06.domain.feed.service.impl.FeedLikeServiceImpl;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FeedLikeServiceImplTest {

  @InjectMocks
  private FeedLikeServiceImpl feedLikeService;

  @Mock
  private FeedRepository feedRepository;
  @Mock
  private AuthService authService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private FeedLikeRepository feedLikeRepository;

  private final UUID feedId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final Feed mockFeed = mock(Feed.class);
  private final User mockUser = mock(User.class);
  private final FeedLike mockFeedLike = mock(FeedLike.class);

  @Nested
  @DisplayName("likeFeed() 테스트")
  class LikeFeedTest {

    @Test
    @DisplayName("성공적으로 피드 좋아요를 등록한다.")
    void likeFeed_success() {
      // given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
      given(feedLikeRepository.existsByFeedAndUser(mockFeed, mockUser)).willReturn(false);

      // when
      feedLikeService.likeFeed(feedId);

      // then
      verify(feedLikeRepository).save(any(FeedLike.class));
      verify(mockFeed).like();
    }

    @Test
    @DisplayName("이미 좋아요 누른 피드에 중복 좋아요를 시도하면 예외가 발생한다.")
    void likeFeed_alreadyLiked_fail() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
      given(feedLikeRepository.existsByFeedAndUser(mockFeed, mockUser)).willReturn(true);

      // when & then
      assertThrows(OtbooException.class, () -> feedLikeService.likeFeed(feedId));
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외가 발생한다.")
    void likeFeed_userNotFound_fail() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      assertThrows(OtbooException.class, () -> feedLikeService.likeFeed(feedId));
    }

    @Test
    @DisplayName("피드가 존재하지 않으면 예외가 발생한다.")
    void likeFeed_feedNotFound_fail() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(feedId)).willReturn(Optional.empty());

      assertThrows(OtbooException.class, () -> feedLikeService.likeFeed(feedId));
    }
  }

  @Nested
  @DisplayName("unlikeFeed() 테스트")
  class UnlikeFeedTest {

    @Test
    @DisplayName("성공적으로 피드 좋아요를 취소한다.")
    void unlikeFeed_success() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
      given(feedLikeRepository.findByFeedAndUser(mockFeed, mockUser)).willReturn(Optional.of(mockFeedLike));

      feedLikeService.unlikeFeed(feedId);

      verify(feedLikeRepository).delete(mockFeedLike);
      verify(mockFeed).unlike();
    }

    @Test
    @DisplayName("좋아요 엔티티가 존재하지 않으면 예외가 발생한다.")
    void unlikeFeed_notLiked_fail() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(feedId)).willReturn(Optional.of(mockFeed));
      given(feedLikeRepository.findByFeedAndUser(mockFeed, mockUser)).willReturn(Optional.empty());

      assertThrows(OtbooException.class, () -> feedLikeService.unlikeFeed(feedId));
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외가 발생한다.")
    void unlikeFeed_userNotFound_fail() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      assertThrows(OtbooException.class, () -> feedLikeService.unlikeFeed(feedId));
    }

    @Test
    @DisplayName("피드가 존재하지 않으면 예외가 발생한다.")
    void unlikeFeed_feedNotFound_fail() {
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(feedRepository.findById(feedId)).willReturn(Optional.empty());

      assertThrows(OtbooException.class, () -> feedLikeService.unlikeFeed(feedId));
    }
  }


}
