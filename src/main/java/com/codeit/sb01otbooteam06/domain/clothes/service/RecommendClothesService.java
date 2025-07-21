package com.codeit.sb01otbooteam06.domain.clothes.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.RecommendClothesRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendClothesService {

  private final AttributeDefService attributeDefService;

  private final RecommendClothesRepository recommendClothesRepository;
  private final ClothesRepository clothesRepository;

  private final RecommendClothesMapper recommendClothesMapper;
  private final ClothesAttributeRepository clothesAttributeRepository;

  //의상 타입 extra 목록 선언
  private static final List<String> TYPE_LIST = Arrays.asList("OUTER", "UNDERWEAR", "ACCESSORY",
      "SOCKS",
      "HAT", "BAG",
      "SCARF", "ETC");

  //todo 책임이 많아서 저장과 가져오기 분리하면 좋을듯.

  /**
   * 날씨와 사용자 취향을 고려해 추천 의상을 생성해 DB에 저장한다.
   *
   * @param valueData
   * @param user
   * @param weather
   * @return 추천의상 셋 중 하나의 의상 아이디리스트
   */
  @Transactional
  public List<UUID> makeRecommendClothes(int[] valueData, User user, Weather weather) {
    /**
     * 추천 로직. 
     * 1 우선 밸류 및 성별에 일치하는 의상 리스트들을 찾아옴.
     * 2 이후 의상 종류에 따른 상의/원피스/바텀/신발/나머지 리스트를 만들고
     * 3 (상의+바텀 or 원피스 / 바지/ 슈즈) 리스트에서 필수 하나씩 + 랜덤으로 스타일에 맞는 기타 의상을 하나의 추천 의상 셋으로 만듦.
     * 4 추천의상 (테이블에 날씨:유저:추천의상ids리스트)를 n개 저장한다.
     */

    ///db에 있는 유저-날씨에 대한 추천 데이터 삭제
    recommendClothesRepository.deleteByUserAndWeather(user, weather);

    /// 속성값에 일치하는 유저 의상을 모두겟
    // [계절, 두께감, 안감, 따뜻한 정도]
    // 의상리스트
    List<Clothes> clothesList = clothesRepository.findAllByOwnerWithValue(user, valueData);

    // todo:  굳이 필요 x
    //의상 id 리스트
    List<UUID> clothesIds = clothesList.stream()
        .map(Clothes::getId)
        .collect(Collectors.toList());
    //의상 id 리스트에 해당하는 의상속성 중간테이블 로드 (쿼리릍 통해 한번에 가져와 n+1문제 해결)
    List<ClothesAttribute> clothesAttributeList = clothesAttributeRepository.findByClothesIn(
        clothesList);
//        clothesAttributeService.findAttributesByClothesIds(clothesIds);

    /// 추천 의상 세트를 만듦.

    //유저 성별
    Gender gender = user.getProfile().getGender();

    //빈 추천 의상 아이디리스트 선언
    List<UUID> recoClothesIds = new ArrayList<>();

    // 의상 속성 테이블에서 "스타일" 속성의 값을 리스트로 가져옴.
    List<String> styleValues = attributeDefService.getStyleValues();

    //의상 추천 셋 생성
    for (int i = 0; i < 5; i++) {
      recoClothesIds.clear();

      //매 세트마다 스타일을 랜덤 하나 선정
      Random random = new Random();
      String style = styleValues.get(random.nextInt(styleValues.size()));

      //스타일에 맞는 의상리스트 필터링
      List<Clothes> clothesListWithStyle = getClothesByStyle(style, clothesList,
          clothesAttributeList);

      // 의상 목록 풀만들기. 상의/하의/원피스/신발/나머지
      List<Clothes> topList = getClothesTypeList(clothesListWithStyle, "TOP");
      List<Clothes> bottomList = getClothesTypeList(clothesListWithStyle, "BOTTOM");
      List<Clothes> shoesList = getClothesTypeList(clothesListWithStyle, "SHOES");
      List<Clothes> dressList = getClothesTypeList(clothesListWithStyle, "DRESS");

      ///성별에 따른 의상 추천
      // 여자면 랜덤으로 원피스 or 상의+바지 조합
      if (gender == Gender.FEMALE) {
        boolean useDress = ThreadLocalRandom.current().nextBoolean(); // true/false 랜덤 결정
        // 원피스 있으면 원피스 넣기
        if (useDress && !dressList.isEmpty()) {
          addClothesIdIfNotNull(recoClothesIds, pickRandom(dressList));
        } else {
          addClothesIdIfNotNull(recoClothesIds, pickRandom(topList));
          addClothesIdIfNotNull(recoClothesIds, pickRandom(bottomList));
        }
      } else {
        // 여자 아니면 그냥 top + bottom 조합
        addClothesIdIfNotNull(recoClothesIds, pickRandom(topList));
        addClothesIdIfNotNull(recoClothesIds, pickRandom(bottomList));
      }

      //신발선택
      addClothesIdIfNotNull(recoClothesIds, pickRandom(shoesList));

      //나머지 의상에서는, 타입별로 랜덤 하나를 뽑거나 뽑지 않아 추천 의상 아이디리스트에 추가해준다.
      recoClothesIds.addAll(getExtraClothes(clothesListWithStyle));

      // 추천 의상 셋 DB 저장
      recommendClothesRepository.save(
          recommendClothesMapper.toEntity(weather, user, recoClothesIds));
    }

    //추천 리스트 중 첫번째 추천셋 반환
    return recoClothesIds;
  }

  /**
   * 타겟 의상아이디리스트에 id가 null이 아니면 추가한다.
   *
   * @param targetList
   * @param id
   */
  private void addClothesIdIfNotNull(List<UUID> targetList, UUID id) {
    if (id != null) {
      targetList.add(id);
    }
  }

  /**
   * 의상리스트에서 스타일에 맞는 의상을 필터링한다.
   *
   * @param style
   * @param clothesList
   * @param attributeList
   * @return 스타일과 일치하는 의상리스트
   */
  private List<Clothes> getClothesByStyle(String style, List<Clothes> clothesList,
      List<ClothesAttribute> attributeList) {
    // 의상별 스타일 속성 맵 생성 (성능 개선)
    Map<UUID, String> clothesStyleMap = attributeList.stream()
        .filter(attr -> "스타일".equals(attr.getAttributeDef().getName()))
        .collect(Collectors.toMap(
            attr -> attr.getClothes().getId(),
            ClothesAttribute::getValue,
            (v1, v2) -> v1 // 중복 시 첫 번째 값 사용
        ));

    return clothesList.stream()
        .filter(clothes -> {
          String clothesStyle = clothesStyleMap.get(clothes.getId());
          // 스타일 속성이 없거나, 스타일이 일치하는 경우
          return clothesStyle == null || style.equals(clothesStyle);
        })
        .collect(Collectors.toList());

  }

  /**
   * 의상리스트에서 의상 타입(분류)과 일치하는 의상을 필터링한다.
   *
   * @param clothesList
   * @param type
   * @return 타입과 일치하는 의상 리스트
   */
  private List<Clothes> getClothesTypeList(List<Clothes> clothesList, String type) {

    return clothesList.stream()
        .filter(clothes -> clothes.getType().equals(type))
        .collect(Collectors.toList());

  }

  // 의상 리스트에서 랜덤 하나 clothes 의 id를 반환하는 함수
  private UUID pickRandom(List<Clothes> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    int idx = ThreadLocalRandom.current().nextInt(list.size());
    return list.get(idx).getId();
  }

  // 의상 리스트에서 랜덤 하나 clothes 의 id를 뽑거나 뽑지않고 반환하는 함수
  private UUID pickRandomOrNot(List<Clothes> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    // 50% 확률로 뽑지 않음
    if (ThreadLocalRandom.current().nextBoolean()) {
      return null;
    }
    // 50% 확률로 뽑음.
    int idx = ThreadLocalRandom.current().nextInt(list.size());
    return list.get(idx).getId();
  }

  /**
   * 상의/바지/원피스/신발 제외 의상리스트에서 랜덤한 의상의 id리스트를 반환한다.
   *
   * @param clothesList 나머지의상리스트
   * @return 나머지 의상리스트에서 랜덤
   */
  private List<UUID> getExtraClothes(List<Clothes> clothesList) {
    List<UUID> extraClothesIds = new ArrayList<>();

    // 나머지 타입 의상 리스트에서 의상을 뽑거나 뽑지 않는다.
    for (String type : TYPE_LIST) {
      UUID id = pickRandomOrNot(getClothesTypeList(clothesList, type));
      addClothesIdIfNotNull(extraClothesIds, id);
    }

    return extraClothesIds;

  }


}
