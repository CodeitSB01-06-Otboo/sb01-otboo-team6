package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.RecommendClothes;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.RecommendClothesRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RecommendClothesServiceTest {

  @Mock
  private AttributeDefService attributeDefService;

  @Mock
  private RecommendClothesRepository recommendClothesRepository;

  @Mock
  private ClothesRepository clothesRepository;

  @Mock
  private RecommendClothesMapper recommendClothesMapper;

  @Mock
  private ClothesAttributeRepository clothesAttributeRepository;

  @InjectMocks
  private RecommendClothesService recommendClothesService;

  @Mock
  private AttributeDef attributeDef;

  private User user;
  private Weather weather;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // User, Profile, Gender 셋업
    Profile profile = mock(Profile.class);
    when(profile.getGender()).thenReturn(Gender.FEMALE);

    user = mock(User.class);
    when(user.getProfile()).thenReturn(profile);

    weather = mock(Weather.class);
  }

  @Test
  public void testMakeRecommendClothes_의상추천을하고_DB에_저장한다() {
    // given
    int[] valueData = {1, 2, 3, 4};
    when(attributeDef.getName()).thenReturn("스타일");
    List<Clothes> clothesList = new ArrayList<>();
    Clothes clothes1 = mock(Clothes.class);
    UUID clothes1Id = UUID.randomUUID();
    when(clothes1.getId()).thenReturn(clothes1Id);
    when(clothes1.getType()).thenReturn("TOP");

    Clothes clothes2 = mock(Clothes.class);
    UUID clothes2Id = UUID.randomUUID();
    when(clothes2.getId()).thenReturn(clothes2Id);
    when(clothes2.getType()).thenReturn("BOTTOM");

    Clothes clothes3 = mock(Clothes.class);
    UUID clothes3Id = UUID.randomUUID();
    when(clothes3.getId()).thenReturn(clothes3Id);
    when(clothes3.getType()).thenReturn("SHOES");

    Clothes clothes4 = mock(Clothes.class);
    UUID clothes4Id = UUID.randomUUID();
    when(clothes4.getId()).thenReturn(clothes4Id);
    when(clothes4.getType()).thenReturn("DRESS");

    clothesList.add(clothes1);
    clothesList.add(clothes2);
    clothesList.add(clothes3);
    clothesList.add(clothes4);

    when(clothesRepository.findAllByOwnerWithValue(user, valueData)).thenReturn(clothesList);

    List<ClothesAttribute> clothesAttributes = new ArrayList<>();
    // 스타일 속성 세팅
    ClothesAttribute styleAttr1 = mock(ClothesAttribute.class);
    when(styleAttr1.getAttributeDef()).thenReturn(attributeDef);
    when(styleAttr1.getValue()).thenReturn("CASUAL");
    when(styleAttr1.getClothes()).thenReturn(clothes1);
    clothesAttributes.add(styleAttr1);

    when(clothesAttributeRepository.findByClothesIn(clothesList)).thenReturn(clothesAttributes);

    // 스타일 리스트 반환 모킹
    List<String> styleValues = List.of("CASUAL", "FORMAL");
    when(attributeDefService.getStyleValues()).thenReturn(styleValues);

    // Mapper 변환 결과 모킹
    when(recommendClothesMapper.toEntity(any(), any(), any())).thenReturn(
        mock(RecommendClothes.class));

    // when
    List<UUID> result = recommendClothesService.makeRecommendClothes(valueData, user, weather);

    // then
    assertNotNull(result);
    // 적어도 하나의 UUID가 들어있어야 함 (추천 의상 리스트)
    assertFalse(result.isEmpty());

    // Repository deleteByUserAndWeather 호출 확인
    verify(recommendClothesRepository).deleteByUserAndWeather(user, weather);

    // Repository save가 5번 호출되어야 함 (추천 5세트 생성)
    verify(recommendClothesRepository, times(5)).save(any(RecommendClothes.class));

  }

  @Test
  public void testFindRandomByUserAndWeather_날씨와_유저에_대한_추천의상세트_중_랜덤한_하나를_반환한다() {
    // given
    UUID userId = UUID.randomUUID();
    UUID weatherId = UUID.randomUUID();

    RecommendClothes mockRecommendClothes = mock(RecommendClothes.class);

    when(recommendClothesRepository.findRandomByUserAndWeather(userId, weatherId))
        .thenReturn(mockRecommendClothes);

    // when
    RecommendClothes result = recommendClothesService.findRandomByUserAndWeather(userId, weatherId);

    // then
    assertNotNull(result);
    assertEquals(mockRecommendClothes, result);

    verify(recommendClothesRepository, times(1))
        .findRandomByUserAndWeather(userId, weatherId);
  }


}
