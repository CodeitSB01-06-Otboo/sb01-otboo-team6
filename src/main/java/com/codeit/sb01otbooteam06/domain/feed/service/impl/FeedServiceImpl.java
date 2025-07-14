package com.codeit.sb01otbooteam06.domain.feed.service.impl;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.FeedDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedRepository;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedQueryRepository;
import com.codeit.sb01otbooteam06.domain.feed.service.FeedService;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.mapper.WeatherDtoMapper;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import com.codeit.sb01otbooteam06.global.exception.ErrorCode;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

  private final FeedRepository feedRepository;
  private final UserRepository userRepository;
  private final WeatherRepository weatherRepository;
  private final ClothesRepository clothesRepository;
  private final ClothesMapper clothesMapper;
  private final WeatherDtoMapper weatherDtoMapper;
  private final AuthService authService;

  @Override
  @Transactional
  public FeedDto createFeed(FeedCreateRequest request) {

    UUID userId = authService.getCurrentUserId();
    User author = userRepository.findById(userId)
        .orElseThrow(() -> new OtbooException(ErrorCode.USER_NOT_FOUND));

    Weather weather = weatherRepository.findById(request.getWeatherId())
        .orElseThrow(() -> new OtbooException(ErrorCode.WEATHER_NOT_FOUND));

    Feed feed = Feed.of(request.getContent(), author, weather);

    // 의상 연결
    List<Clothes> clothesList = request.getClothesIds().stream()
        .map(clothesId -> clothesRepository.findById(clothesId)
            .orElseThrow(() -> new OtbooException(ErrorCode.CLOTHES_NOT_FOUND)))
        .toList();
    feed.setClothesFeeds(clothesList);
    feedRepository.save(feed);

    return FeedDto.fromEntity(feed, clothesMapper, weatherDtoMapper);
  }

  @Transactional(readOnly = true)
  @Override
  public FeedDto getFeed(UUID feedId) {
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new OtbooException(ErrorCode.FEED_NOT_FOUND));

    return FeedDto.fromEntity(feed, clothesMapper, weatherDtoMapper);
  }

  @Transactional
  @Override
  public FeedDto updateFeed(UUID feedId, FeedUpdateRequest request) {
    // 피드 소유주가 아니면 오류
    UUID userId = authService.getCurrentUserId();
    if (!feedRepository.existsByIdAndUserId(feedId, userId)) {
      throw new OtbooException(ErrorCode.UNAUTHORIZED_FEED_ACCESS);
    }

    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new OtbooException(ErrorCode.FEED_NOT_FOUND));
    feed.updateContent(request.getContent());
    Feed updatedFeed = feedRepository.save(feed);

    return FeedDto.fromEntity(updatedFeed, clothesMapper, weatherDtoMapper);
  }

  @Transactional
  @Override
  public void deleteFeed(UUID feedId) {
    UUID userId = authService.getCurrentUserId();
    if (!feedRepository.existsByIdAndUserId(feedId, userId)) {
      throw new OtbooException(ErrorCode.UNAUTHORIZED_FEED_ACCESS);
    }
    feedRepository.deleteById(feedId);
  }


  @Transactional(readOnly = true)
  @Override
  public FeedDtoCursorResponse getFeedsByCursor(String keyword, SkyStatus skyStatus,
      PrecipitationType precipitationType,
      Instant cursorCreatedAt, UUID cursorId, Long cursorLikeCount, int size, String sortBy) {

    keyword = (keyword == null || keyword.isBlank()) ? null : keyword;
    PageRequest pageReq = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, sortBy));

    // 커서 값 설정 (정렬 기준에 따라)
    Object cursorValue = "likeCount".equalsIgnoreCase(sortBy) ? cursorLikeCount : cursorCreatedAt;

    Page<Feed> resultPage = feedRepository.findFeedsByCursorAndSort(
        keyword,
        skyStatus,
        precipitationType,
        cursorValue,
        cursorId,
        pageReq
    );

    List<FeedDto> data = resultPage.getContent().stream()
        .map(feed -> FeedDto.fromEntity(feed, clothesMapper, weatherDtoMapper))
        .toList();

    boolean hasNext = resultPage.hasNext(); // 정확한 hasNext 판단
    String nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext && !data.isEmpty()) {
      Feed last = resultPage.getContent().get(data.size() - 1);
      nextIdAfter = last.getId();
      nextCursor = "likeCount".equalsIgnoreCase(sortBy)
          ? String.valueOf(last.getLikeCount())
          : last.getCreatedAt().toString();
    }

    long totalCount = feedRepository.countByFilters(keyword, skyStatus, precipitationType);

    return new FeedDtoCursorResponse(
        data,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        "DESC"
    );
  }
}
