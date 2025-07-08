package com.codeit.sb01otbooteam06.domain.clothes.service;


import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.OotdDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.RecommendationDto;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final ClothesService clothesService;

  //todo: 서비스? 웨더 확인해보고 추후 변경
  private final WeatherRepository weatherRepository;

  private final UserRepository userRepository;

  private final ClothesMapper clothesMapper;
  private final ClothesRepository clothesRepository;

  //TODO: 의상 추천 알고리즘
  //날씨 데이터, 사용자가 등록한 의상, 프로필 정보를 활용하여 의상을 추천
  @Transactional
  public RecommendationDto create(UUID weatherId) {

    //todo: 유저 아이디 획득

    User user = userRepository.findByEmail("admin@example.com")
        .orElseThrow(() -> new NoSuchElementException());

    /**날씨 데이터
     * 1. 기온
     * 2. 바람
     * 3. 날씨
     * 4. 습도
     *
     * 의상 속성
     * 1. 두께감
     * 2. 계절
     * 3. 안감 
     * 4. 따뜻한 정도
     * */

//    //날씨 데이터 가져오기
//    Weather weather = weatherRepository.findById(weatherId)
//        .orElseThrow(() -> new WeatherNotFoundException());

    //의상 데이터 가져오기
    List<Clothes> clothesList = clothesRepository.findAllByOwner(user);

    ///날씨 추천 로직
    /**
     * 1. 날씨 데이터 가져옴
     * 2. gen-ai에게 프롬포팅해서 결과값(의상 속성중 날씨 관련)을 수치화
     * 3. 해당 수치를 토대로 의상리스트에서, 속성에서 값이 높은것들 산출
     * 4. 의상 추천 조합 생성해 DB저장
     *
     * TODO: 1)프로필 위치로, 배치 작업으로 DB저장해 꺼내써 빠른 사용자 속도 경험 제공,
     *       2)새로운 위치시 즉시호출
     */

    //gen-ai 클라이언트
    Client client = new Client();

    long startTime = System.currentTimeMillis();

    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-2.5-flash",
            "Explain how AI works in a few words",
            types.);

    long endTime = System.currentTimeMillis();

    System.out.println("response = " + response.text());
    System.out.println("응답 생성 시간: " + (endTime - startTime) + " ms");

    //todo: 시간이 소요될 것으로 예상되어 우선 임시 데이터 던지게하기
    List<OotdDto> result = new ArrayList<>();
    result.add(makeDummyOotdDto("상의", "TOP"));
    result.add(makeDummyOotdDto("하의", "BOTTOM"));
    result.add(makeDummyOotdDto("아우터", "OUTER"));
    result.add(makeDummyOotdDto("모자", "HAT"));
    return new RecommendationDto(weatherId, user.getId(), result);
  }


  //admin의 더미 Ootd데이터 생성
  private OotdDto makeDummyOotdDto(String name, String type) {

    User user = userRepository.findByEmail("admin@example.com")
        .orElseThrow(() -> new NoSuchElementException());

    Clothes clothes = new Clothes(user, name, type, null);

    //의상 저장
    clothesService.create(new ClothesCreateRequest(
        user.getId(),
        name, type,
        Collections.emptyList()

    ), null);

    return OotdDto.toDto(clothesMapper.toDto(clothes));

  }

}
