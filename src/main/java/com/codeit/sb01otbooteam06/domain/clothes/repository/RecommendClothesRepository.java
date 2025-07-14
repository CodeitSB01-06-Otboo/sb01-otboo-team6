package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.RecommendClothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecommendClothesRepository extends JpaRepository<RecommendClothes, UUID> {

  boolean existsByUserAndWeather(User user, Weather weather);

  void deleteByUserAndWeather(User user, Weather weather);

  // weather-user 에 해당하는 추천 셋 중 랜덤 하나를 리턴
  @Query("""
      SELECT rc.clothesIds
      FROM RecommendClothes rc
      WHERE rc.user = :user AND rc.weather = :weather
      ORDER BY function('RANDOM')
      LIMIT 1
      """)
  List<UUID> findClothesIdsByUserAndWeather(User user, Weather weather);
}
