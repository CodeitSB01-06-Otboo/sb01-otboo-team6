package com.codeit.sb01otbooteam06.domain.feed.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.FeedDto;
import com.codeit.sb01otbooteam06.domain.feed.dto.response.FeedDtoCursorResponse;
import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedLikeRepository;
import com.codeit.sb01otbooteam06.domain.feed.repository.FeedRepository;
import com.codeit.sb01otbooteam06.domain.feed.service.impl.FeedServiceImpl;
import com.codeit.sb01otbooteam06.domain.notification.service.NotificationService;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherSummaryDto;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.mapper.WeatherDtoMapper;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import com.codeit.sb01otbooteam06.global.exception.OtbooException;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FeedServiceImplTest {

  @InjectMocks
  private FeedServiceImpl feedService;

  @Mock
  private FeedRepository feedRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private WeatherRepository weatherRepository;
  @Mock
  private ClothesRepository clothesRepository;
  @Mock
  private ClothesMapper clothesMapper;
  @Mock
  private WeatherDtoMapper weatherDtoMapper;
  @Mock
  private AuthService authService;
  @Mock
  private FeedLikeRepository feedLikeRepository;
  @Mock
  private NotificationService notificationService;

  private final UUID userId = UUID.randomUUID();
  private final UUID feedId = UUID.randomUUID();
  private final UUID weatherId = UUID.randomUUID();
  private final UUID clothesId = UUID.randomUUID();

  Weather mockWeather = mock(Weather.class);
  Clothes mockClothes = mock(Clothes.class);
  ClothesDto mockClothesDto = mock(ClothesDto.class);
  User user = EntityProvider.createTestUser();

  @Nested
  @DisplayName("피드 생성 메서드 테스트")
  class createdFeed {

    FeedCreateRequest request = FeedCreateRequest.builder().authorId(userId).weatherId(weatherId)
        .clothesIds(List.of(clothesId)).content("내용").build();

    User receiver = EntityProvider.createTestUser();

    @DisplayName("유저가 있는 곳에 날씨 정보를 통해 옷을 추천받고 추천 의상을 통해 피드를 생성한다.")
    @Test
    void createFeed_Success() {
      //given
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(weatherRepository.findById(weatherId)).willReturn(Optional.of(mockWeather));
      given(clothesRepository.findById(clothesId)).willReturn(Optional.of(mockClothes));
      given(authService.getCurrentUserId()).willReturn(userId);
      given(clothesMapper.toDto(any(Clothes.class))).willReturn(mockClothesDto);
      given(feedLikeRepository.existsByFeedAndUser(any(), any())).willReturn(false);

      willDoNothing().given(notificationService)
          .notifyFolloweePostedFeed(any(User.class), anyString());

      //when
      FeedDto result = feedService.createFeed(request);

      //then
      assertNotNull(result);
      verify(feedRepository).save(any(Feed.class));
      verify(notificationService).notifyFolloweePostedFeed(eq(user), eq("내용"));

    }

    @DisplayName("피드 생성 시 인증된 유저가 아니면 피드 생성에 실패한다.")
    @Test
    void createFeed_Not_Found_User_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.empty());
      //when, then
      assertThrows(OtbooException.class, () -> feedService.createFeed(request));
    }

    @DisplayName("피드 생성 시 날씨 데이터가 없으면 피드 생성에 실패한다.")
    @Test
    void createFeed_Not_Found_Weather_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(weatherRepository.findById(weatherId)).willReturn(Optional.empty());

      //when , then
      assertThrows(OtbooException.class, () -> feedService.createFeed(request));
    }

    @DisplayName("피드 생성 시 옷 데이터가 없으면 피드 생성에 실패한다.")
    @Test
    void createFeed_Not_Found_Clothes_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(weatherRepository.findById(weatherId)).willReturn(Optional.of(mockWeather));
      given(clothesRepository.findById(clothesId)).willReturn(Optional.empty());

      //when, then
      assertThrows(OtbooException.class, () -> feedService.createFeed(request));
    }

  }

  @Nested
  @DisplayName("피드 단건 조회 테스트")
  class getFeed {

    @DisplayName("피드를 단건 조회해서 피드 하나를 반환 받는다.")
    @Test
    void getFeed_Success() {

      //given
      Weather weather = mock(Weather.class);
      Clothes clothes = mock(Clothes.class);
      Feed feed = Feed.of("내용", user, weather);
      feed.setClothesFeeds(List.of(clothes));

      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(authService.getCurrentUserId()).willReturn(userId);
      given(clothesMapper.toDto(clothes)).willReturn(mock(ClothesDto.class));
      given(weatherDtoMapper.toSummaryDto(weather)).willReturn(mock(WeatherSummaryDto.class));
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedLikeRepository.existsByFeedAndUser(any(), any())).willReturn(false);

      //when
      FeedDto result = feedService.getFeed(feedId);

      //then
      assertNotNull(result);
      assertEquals("내용", result.getContent());
    }

    @Test
    @DisplayName("피드 단건 조회시, 피드가 존재하지 않으면 피드 하나를 반환할 수 없다.")
    void getFeed_Not_Found_Feed_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedRepository.findById(feedId)).willReturn(Optional.empty());
      //when, then
      assertThrows(OtbooException.class, () -> feedService.getFeed(feedId));
    }

  }

  @Nested
  @DisplayName("피드 수정 테스트")
  class updateFeed {

    FeedUpdateRequest request = FeedUpdateRequest.builder().content("test").build();

    Feed feed = Feed.of("내용", user, mockWeather);

    @DisplayName("피드를 소유하고 있는 유저이고 피드가 존재한다면 피드를 수정한다.")
    @Test
    void updateFeed_Success() {
      //given
      feed.setClothesFeeds(List.of(mockClothes));

      given(authService.getCurrentUserId()).willReturn(userId);
      given(feedRepository.findById(feedId)).willReturn(Optional.of(feed));
      given(feedRepository.save(any(Feed.class))).willReturn(feed);
      given(feedRepository.existsByIdAndUserId(feedId, userId)).willReturn(true);
      given(clothesMapper.toDto(any(Clothes.class))).willReturn(mockClothesDto);
      given(feedLikeRepository.existsByFeedAndUser(any(), any())).willReturn(false);

      //when
      FeedDto feedDto = feedService.updateFeed(feedId, request);

      //then
      assertNotNull(feedDto);
      assertEquals("test", feedDto.getContent());

      ArgumentCaptor<Feed> captor = ArgumentCaptor.forClass(Feed.class);
      verify(feedRepository).save(captor.capture());
      assertEquals("test", captor.getValue().getContent());

    }

    @DisplayName("피드를 수정 할때, 피드 소유자가 아니면 수정 할 수 없다.")
    @Test
    void updateFeed_Not_Owner_Feed_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(feedRepository.existsByIdAndUserId(feedId, userId)).willReturn(false);

      //when, then
      assertThrows(OtbooException.class, () -> feedService.updateFeed(feedId, request));
    }

    @DisplayName("피드를 수정 할때, 피드 소유자이지만 해당 피드가 없으면 수정 할 수 없다.")
    @Test
    void updateFeed_Not_Found_Feed_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(feedRepository.existsByIdAndUserId(feedId, userId)).willReturn(true);
      given(feedRepository.findById(feedId)).willReturn(Optional.empty());

      //when, then
      assertThrows(OtbooException.class, () -> feedService.updateFeed(feedId, request));
    }

  }

  @Nested
  @DisplayName("피드 삭제 테스트")
  class deleteFeed {

    @DisplayName("해당 피드 소유자 일때, 피드를 삭제한다.")
    @Test
    void deleteFeed_Success() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(feedRepository.existsByIdAndUserId(feedId, userId)).willReturn(true);

      //when
      feedService.deleteFeed(feedId);

      //then
      verify(feedRepository).deleteById(feedId);
    }

    @DisplayName("피드 삭제 시 해당 피드 소유자가 아니면 삭제 할 수 없다.")
    @Test
    void deleteFeed_Not_Owner_Feed_ThrowsException() {
      //given
      given(authService.getCurrentUserId()).willReturn(userId);
      given(feedRepository.existsByIdAndUserId(feedId, userId)).willReturn(false);

      //when, then
      assertThrows(OtbooException.class, () -> feedService.deleteFeed(feedId));

    }

  }

  @Nested
  @DisplayName("피드 커서페이지네이션 테스트")
  class getCursorFeeds {

    String keyword = "코디";
    SkyStatus skyStatus = SkyStatus.CLEAR;
    PrecipitationType precipitationType = PrecipitationType.NONE;
    Instant cursorCreatedAt = Instant.parse("2025-07-16T10:51:56.781853Z");
    UUID cursorId = UUID.randomUUID();
    Long likeCursor = 100L;
    int size = 2;

    Feed feed = Feed.of("내용", user, mockWeather);


    @DisplayName("필터 조건이 주어졌을 때 피드 리스트를 커서 기반으로 반환한다.")
    @Test
    void getFeedsByCursor_Success() {
      // given
      Feed feed1 = Feed.of("OOTD1", user, mockWeather);
      feed1.setClothesFeeds(List.of(mockClothes));
      feed1.like(); // likeCount = 1
      Feed feed2 = Feed.of("OOTD2", user, mockWeather);
      feed2.setClothesFeeds(List.of(mockClothes));
      feed2.like();
      feed2.like(); // likeCount = 2

      String sortBy = "likeCount";
      List<Feed> feeds = List.of(feed1, feed2);
      Page<Feed> page = new PageImpl<>(feeds,
          PageRequest.of(0, size, Sort.by(sortBy).descending()), 5);

      given(feedRepository.findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(likeCursor),
          eq(cursorId),
          any(PageRequest.class)
      )).willReturn(page);

      given(feedRepository.countByFilters(keyword, skyStatus, precipitationType))
          .willReturn(20L);

      given(clothesMapper.toDto(any(Clothes.class))).willReturn(mock(ClothesDto.class));
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));

      // when
      FeedDtoCursorResponse result = feedService.getFeedsByCursor(
          keyword, skyStatus, precipitationType,
          cursorCreatedAt, cursorId, likeCursor, size, sortBy
      );

      // then
      assertNotNull(result);
      assertEquals(2, result.getData().size());
      assertEquals("likeCount", result.getSortBy());
      assertEquals("DESC", result.getSortDirection());
      assertTrue(result.isHasNext());
      assertEquals(20L, result.getTotalCount());
      assertEquals(String.valueOf(feed2.getLikeCount()), result.getNextCursor());
      assertEquals(feed2.getId(), result.getNextIdAfter());

      verify(feedRepository).findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(likeCursor),
          eq(cursorId),
          any(PageRequest.class)
      );
    }


    @DisplayName("hasNext가 true이고 데이터가 존재하면 nextCursor와 nextIdAfter가 포함된다.")
    @Test
    void getFeedsByCursor_WithNextPageInfo_Success() {
      // given
      UUID lastFeedId = UUID.randomUUID();
      long lastFeedLikeCount = 123L;
      String sortBy = "likeCount";

      Feed feed1 = Feed.of("OOTD1", user, mockWeather);
      feed1.setClothesFeeds(List.of(mockClothes));
      ReflectionTestUtils.setField(feed1, "id", UUID.randomUUID());

      Feed feed2 = Feed.of("OOTD2", user, mockWeather);
      feed2.setClothesFeeds(List.of(mockClothes));
      ReflectionTestUtils.setField(feed2, "id", lastFeedId);
      ReflectionTestUtils.setField(feed2, "likeCount", lastFeedLikeCount);

      List<Feed> feeds = List.of(feed1, feed2);
      Page<Feed> page = new PageImpl<>(feeds, PageRequest.of(0, size), 5); // hasNext = true

      given(feedRepository.findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(likeCursor),
          eq(cursorId),
          any(PageRequest.class)
      )).willReturn(page);

      given(feedRepository.countByFilters(keyword, skyStatus, precipitationType)).willReturn(5L);
      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      lenient().when(clothesMapper.toDto(any())).thenReturn(mockClothesDto);
      lenient().when(weatherDtoMapper.toDto(any())).thenReturn(mock(WeatherDto.class));

      // when
      FeedDtoCursorResponse result = feedService.getFeedsByCursor(
          keyword, skyStatus, precipitationType,
          cursorCreatedAt, cursorId, likeCursor, size, sortBy
      );

      // then
      assertNotNull(result);
      assertTrue(result.isHasNext());
      assertEquals(String.valueOf(lastFeedLikeCount), result.getNextCursor());
      assertEquals(lastFeedId, result.getNextIdAfter());
      assertEquals(5L, result.getTotalCount());
    }

    @DisplayName("keyword가 null이면 전체 피드를 반환한다.")
    @Test
    void getFeedsByCursor_WithNullKeyword() {
      // given
      String keyword = null;
      Feed feed = Feed.of("내용", user, mockWeather);
      feed.setClothesFeeds(List.of(mockClothes));
      String sortBy = "likeCount";

      Page<Feed> page = new PageImpl<>(List.of(feed));

      given(feedRepository.findFeedsByCursorAndSort(
          isNull(),
          eq(skyStatus),
          eq(precipitationType),
          eq(likeCursor),
          eq(cursorId),
          any(PageRequest.class)
      )).willReturn(page);

      given(feedRepository.countByFilters(
          isNull(),
          eq(skyStatus),
          eq(precipitationType)
      )).willReturn(1L);

      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(clothesMapper.toDto(any())).willReturn(mock(ClothesDto.class));

      // when
      FeedDtoCursorResponse result = feedService.getFeedsByCursor(
          keyword, skyStatus, precipitationType, cursorCreatedAt, cursorId, likeCursor, size, sortBy
      );

      // then
      assertNotNull(result);
      assertEquals(1, result.getData().size());
    }

    @DisplayName("sortBy가 likeCount일 때 cursorLikeCount가 사용되어야 한다")
    @Test
    void getFeedsByCursor_CursorValue_LikeCountUsed() {
      // given
      String sortBy = "likeCount";

      Feed feed = Feed.of("OOTD1", user, mockWeather);
      Page<Feed> page = new PageImpl<>(List.of(feed));

      given(feedRepository.findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(likeCursor),
          eq(cursorId),
          any(PageRequest.class)
      )).willReturn(page);

      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedRepository.countByFilters(keyword, skyStatus, precipitationType)).willReturn(1L);

      // when
      FeedDtoCursorResponse result = feedService.getFeedsByCursor(
          keyword, skyStatus, precipitationType,
          cursorCreatedAt, cursorId, likeCursor, size, sortBy
      );

      // then
      assertNotNull(result);
      assertEquals("likeCount", result.getSortBy());
      verify(feedRepository).findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(likeCursor),
          eq(cursorId),
          any(PageRequest.class)
      );
    }

    @DisplayName("sortBy가 likeCount가 아닐 때 cursorCreatedAt이 사용되어야 한다")
    @Test
    void getFeedsByCursor_CursorValue_CreatedAtUsed() {
      // given
      String sortBy = "createdAt";

      Feed feed = Feed.of("OOTD2", user, mockWeather);
      Page<Feed> page = new PageImpl<>(List.of(feed));

      given(feedRepository.findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(cursorCreatedAt),
          eq(cursorId),
          any(PageRequest.class)
      )).willReturn(page);

      given(authService.getCurrentUserId()).willReturn(userId);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(feedRepository.countByFilters(keyword, skyStatus, precipitationType)).willReturn(1L);

      // when
      FeedDtoCursorResponse result = feedService.getFeedsByCursor(
          keyword, skyStatus, precipitationType,
          cursorCreatedAt, cursorId, likeCursor, size, sortBy
      );

      // then
      assertNotNull(result);
      assertEquals("createdAt", result.getSortBy());
      verify(feedRepository).findFeedsByCursorAndSort(
          eq(keyword),
          eq(skyStatus),
          eq(precipitationType),
          eq(cursorCreatedAt),
          eq(cursorId),
          any(PageRequest.class)
      );
    }
  }
}
