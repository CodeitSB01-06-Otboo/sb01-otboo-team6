package com.codeit.sb01otbooteam06.domain.clothes.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.RecommendClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.RecommendClothesRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendClothesService {

  private final RecommendClothesRepository recommendClothesRepository;
  private final ClothesRepository clothesRepository;

  private final RecommendClothesMapper recommendClothesMapper;

  @Transactional
  public List<UUID> makeRecommendClothes(int[] valueData, User user, Weather weather) {
    /**
     * 1 우선 가중치에 일치하는 의상 리스트들을 찾아옴.
     * 2 이후 리스트로 상의풀 (성별에 따라 원피스)/하의풀 /신발풀 만들고   필수 + 계절에 따라 아우터
     * 3 성별에 따라 원피스 풀 생성?
     * 4 3액세서리, 양말, 모자, 가방, 스카프는 스타일이 맞으면 넣어줌.
     * 5 그 풀에서 하나씩 랜덤 or 스타일 매칭으로 뽑아서, 의상 추천 코디 셋을 만든다.
     *
     *
     */

    //db에 있는 유저-날씨에 대한 추천 데이터 삭제
    recommendClothesRepository.deleteByUserAndWeather(user, weather);

    //유저에 대한 의상을 속성값에 일치하는 것을 모두겟
    // [계절, 두께감, 안감, 따뜻한 정도]
    List<Clothes> clothesList = clothesRepository.findAllByOwnerWithValue(user, valueData);

    // todo:  여성-남성에 따른 분류 (공용 기본으로, 반대 성별을 제거하는거로)

    /// 추천 의상 세트를 만듦.
    // 의상 목록 풀만들기. 상의/하의/원피스/신발    +기타(모두
    List<Clothes> topList = getClothesTypeList(clothesList, "TOP");
    List<Clothes> bottomList = getClothesTypeList(clothesList, "BOTTOM");
    List<Clothes> shoesList = getClothesTypeList(clothesList, "SHOES");
    //List<Clothes> dressList = getClothesTypeList(clothesList, "DRESS");
    //Todo: 기타

    //풀에서 하나씩 뽑아 추천 의상 세트를 만든다. (상의, 바지, 신발, +악세)
    //의상 아이디리스트
    List<UUID> idSet = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      idSet.clear();

      idSet.add(pickRandom(topList));
      idSet.add(pickRandom(bottomList));
      idSet.add(pickRandom(shoesList));

      // 추천 의상 셋 저장
      recommendClothesRepository.save(recommendClothesMapper.toEntity(weather, user, idSet));
    }

    //추천 리스트 중 첫번째 추천셋 반환
    return idSet;
  }

  // 의상리스트에서, 타입에 맞는 의상리스트를 반환
  private List<Clothes> getClothesTypeList(List<Clothes> clothesList, String type) {

    return clothesList.stream()
        .filter(clothes -> clothes.getType().equals(type))
        .collect(Collectors.toList());

  }

  // 의상 리스트에서 랜덤 하나 clothes 의 id를 반환하는 헬퍼 함수
  private UUID pickRandom(List<Clothes> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    int idx = ThreadLocalRandom.current().nextInt(list.size());
    return list.get(idx).getId();
  }


}
