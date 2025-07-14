package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.RecommendClothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendClothesRepository extends JpaRepository<RecommendClothes, UUID> {

  boolean existsByUserAndWeather(User user, Weather weather);

  void deleteByUserAndWeather(User user, Weather weather);

  //  todo : 문제잇음.
  // weather-user 에 해당하는 추천 셋 중 랜덤 하나를 리턴
  @Query(value = """
      SELECT *
      FROM recommend_clothes rc
      WHERE rc.user_id = :#{#user.id} AND rc.weather_id = :#{#weather.id}
      ORDER BY RANDOM()
      LIMIT 1
      """, nativeQuery = true)
  RecommendClothes findRandomByUserAndWeather(@Param("user") User user,
      @Param("weather") Weather weather);

}
