package com.codeit.sb01otbooteam06.domain.clothes.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.RecommendClothesRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendClothesService {

  private final RecommendClothesRepository recommendClothesRepository;
  private final ClothesRepository clothesRepository;

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

    //유저에 대한 의상을 속성값에 일치하는 것을 겟
    // [계절, 두께감, 안감, 따뜻한 정도]
    List<Clothes> clothesList = clothesRepository.findAllByOwnerWithValue(user, valueData);

    // todo:  여성-남성에 따른 분류 (공용 기본으로, 반대 성별을 제거하는거로)
    // 2. 가중치에 해당하는 idx의 의상들만 셀렉
    //

    // 추천 의상 셋 저장

    return recommendClothesRepository.findClothesIdsByUserAndWeather(user, weather);
  }


}
