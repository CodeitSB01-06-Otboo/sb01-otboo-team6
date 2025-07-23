package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.auth.service.AuthService;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.RecommendClothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.OotdDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.RecommendationDto;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.CustomClothesUtils;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.profile.exception.ProfileNotFoundException;
import com.codeit.sb01otbooteam06.domain.profile.repository.ProfileRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import com.codeit.sb01otbooteam06.domain.weather.exception.WeatherNotFoundException;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

class RecommendServiceTest {

  @Value("${google.api.key}")
  private String apiKey;

  @InjectMocks
  RecommendService recommendService;

  @Mock
  ClothesService clothesService;

  @Mock
  ClothesCacheService clothesCacheService;

  @Mock
  AuthService authService;

  @Mock
  RecommendClothesService recommendClothesService;

  @Mock
  WeatherRepository weatherRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  ProfileRepository profileRepository;

  @Mock
  ClothesAttributeRepository clothesAttributeRepository;

  @Mock
  CustomClothesUtils customClothesUtils;

  @Mock
  CacheManager cacheManager;

  @Mock
  Cache cache;

  @Mock
  private SkyStatus skyStatus;

  @Mock
  private Temperature temperature;

  @Mock
  private Wind wind;


  private User user;
  private Profile profile;
  private Clothes clothes1;
  private Clothes clothes2;
  private Weather weather;
  private Location location;
  private ClothesDto mockClothesDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    user = EntityProvider.createTestUser();
    profile = EntityProvider.createTestProfile(user);
    clothes1 = EntityProvider.createCustomTestClothes(user, "옷1", "TOP", "url");
    clothes2 = EntityProvider.createCustomTestClothes(user, "옷2", "TOP", "url");
    location = Location.from(37.12345, 127.56789, 123, 456);
    weather = Weather.from(Instant.now(), Instant.now(), location);
    mockClothesDto = mock(ClothesDto.class);

    // userId 세팅
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());

  }

  @Test
  void recommend_캐시된_의상_수_일치하면_캐시된_추천_반환() {
    UUID userId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    RecommendClothes recommendClothes = mock(RecommendClothes.class);
    List<UUID> recommendClothesIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    when(recommendClothes.getClothesIds()).thenReturn(recommendClothesIds);

    // mocks
    when(authService.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(weather));
    when(cacheManager.getCache("userClothesCount")).thenReturn(cache);
    when(cache.get(userId, Integer.class)).thenReturn(2);
    when(clothesService.getUserClothesCount(userId)).thenReturn(2);
    when(recommendClothesService.findRandomByUserAndWeather(userId, weatherId)).thenReturn(
        recommendClothes);

    //캐시된 의상 수
    int currentClothesCount = 2;
    when(clothesCacheService.getPageUserClothesCountWithCache(null, userId)).thenReturn(
        currentClothesCount);
    when(clothesService.getUserClothesCount(userId)).thenReturn(currentClothesCount);

    // prepare data for getOotdDtos
    List<Clothes> clothesList = List.of(clothes1, clothes2);
    when(clothesService.findAllById(recommendClothesIds)).thenReturn(clothesList);
    when(clothesAttributeRepository.findByClothesIn(clothesList)).thenReturn(
        Collections.emptyList());

    when(customClothesUtils.makeClothesDto(any(Clothes.class), anyList())).thenReturn(
        mockClothesDto);
    when(customClothesUtils.makeClothesDto(any(), anyList())).thenAnswer(i -> {
      return new OotdDto(UUID.randomUUID(), "옷" + i, "url", "TOP", List.of());
    });

    // test
    RecommendationDto result = recommendService.recommend(weatherId);

    assertThat(result).isNotNull();
    assertThat(result.weatherId()).isEqualTo(weatherId);
    assertThat(result.userId()).isEqualTo(userId);
    assertThat(result.clothes()).isNotNull();
  }

  @Test
  void recommend_유저_없으면_예외_던짐() {
    UUID userId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    when(authService.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> recommendService.recommend(weatherId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void recommend_날씨정보_없으면_예외_던짐() {
    UUID userId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    when(authService.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(weatherRepository.findById(weatherId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> recommendService.recommend(weatherId))
        .isInstanceOf(WeatherNotFoundException.class);
  }

  @Test
  void getWeatherData_프로필_없으면_예외_던짐() throws Exception {
    when(profileRepository.findById(user.getId())).thenReturn(Optional.empty());

    Method method = recommendService.getClass()
        .getDeclaredMethod("getWeatherData", Weather.class, User.class);
    method.setAccessible(true);  // 여기 추가

    assertThatThrownBy(() -> method.invoke(recommendService, weather, user))
        .hasCauseInstanceOf(ProfileNotFoundException.class);
  }

  @Test
  void getWeatherData_올바른_날씨_데이터_배열_반환() {
    user = Mockito.mock(User.class);
    weather = Mockito.mock(Weather.class);
    profile = Mockito.mock(Profile.class);

    UUID userId = UUID.randomUUID();
    when(user.getId()).thenReturn(userId);

    // profileRepository에서 profile 반환
    when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
    when(profile.getTemperatureSensitivity()).thenReturn(3);  // 정수로 세팅

    // weather mock 값 세팅
    Instant forecastAt = Instant.parse("2025-07-22T10:15:30.00Z");
    when(weather.getForecastAt()).thenReturn(forecastAt);
    when(weather.getSkyStatus()).thenReturn(skyStatus);
    when(skyStatus.getCode()).thenReturn(1);
    when(weather.getTemperature()).thenReturn(temperature);
    when(temperature.getCurrent()).thenReturn(25.0);
    when(weather.getHumidity()).thenReturn(60.0);
    when(weather.getWind()).thenReturn(wind);
    when(wind.getSpeed()).thenReturn(3.5);

    double[] result = recommendService.getWeatherData(weather, user);

    assertThat(result).hasSize(6);
    assertThat(result[0]).isEqualTo(7);   // 7월
    assertThat(result[1]).isEqualTo(1);   // 하늘상태 코드
    assertThat(result[2]).isEqualTo(25.0);
    assertThat(result[3]).isEqualTo(60.0);
    assertThat(result[4]).isEqualTo(3.5);
    assertThat(result[5]).isEqualTo(3);   // 온도 민감도 (정수)
  }

  @Test
  void create_추천_의상_ID_목록_반환() {
    // given
    User mockUser = mock(User.class);
    Weather mockWeather = mock(Weather.class);

    double[] mockWeatherData = {7, 1, 25.0, 60.0, 3.5, 2}; // 임의의 날씨 데이터
    int[] mockWeightData = {1, 2, 3, 4}; // 임의의 가중치 데이터
    List<UUID> expectedRecommendIds = List.of(UUID.randomUUID());

    // getWeatherData 메서드 mocking (private 메서드라면 spy 써야함)
    RecommendService spyService = Mockito.spy(recommendService);

    doReturn(mockWeatherData).when(spyService).getWeatherData(mockWeather, mockUser);
    doReturn(mockWeightData).when(spyService).getValueByAi(mockWeatherData);

    // recommendClothesService mocking
    when(recommendClothesService.makeRecommendClothes(mockWeightData, mockUser, mockWeather))
        .thenReturn(expectedRecommendIds);

    // when
    List<UUID> actualRecommendIds = spyService.create(mockUser, mockWeather);

    // then
    assertThat(actualRecommendIds).isEqualTo(expectedRecommendIds);
    verify(spyService).getWeatherData(mockWeather, mockUser);
    verify(spyService).getValueByAi(mockWeatherData);
    verify(recommendClothesService).makeRecommendClothes(mockWeightData, mockUser, mockWeather);
  }


}
