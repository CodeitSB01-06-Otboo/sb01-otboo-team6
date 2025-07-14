package com.codeit.sb01otbooteam06.domain.feed.service.impl;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.entity.FeedLike;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedLikeRepository;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedRepository;
import com.codeit.sb01otbooteam06.domain.feed.service.FeedLikeService;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedLikeServiceImpl implements FeedLikeService {

  private final FeedLikeRepository feedLikeRepository;
  private final UserRepository userRepository;
  private final FeedRepository feedRepository;
  private final AuthService authService;


  @Override
  @Transactional
  public void likeFeed(UUID feedId) {
    UUID userId = authService.getCurrentUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));

    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new OtbooException(ErrorCode.FEED_NOT_FOUND));

    // 좋아요 중복 로직
    if (feedLikeRepository.existsByFeedAndUser(feed, user)) {
      throw new OtbooException(ErrorCode.ILLEGAL_ARGUMENT_ERROR);
    }

    // 좋아요 생성 & DB 저장
    FeedLike like = new FeedLike(feed, user);
    feedLikeRepository.save(like);

    //피드 카운드 증가 -> dirty checking 으로 자동 반영
    feed.like();

    //todo : 나중에 좋아요 누를때 알림 가도록 설정

  }

  @Transactional
  @Override
  public void unlikeFeed(UUID feedId) {
    UUID userId = authService.getCurrentUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));

    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new OtbooException(ErrorCode.FEED_NOT_FOUND));

    // 기존 좋아요 엔티티 조회
    FeedLike like = feedLikeRepository.findByFeedAndUser(feed, user)
        .orElseThrow(() -> new OtbooException(ErrorCode.ILLEGAL_ARGUMENT_ERROR));

    // 좋아요 취소
    feedLikeRepository.delete(like);

    // 피드 좋아요 수 감소
    feed.unlike();

  }
}
