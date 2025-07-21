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

  @Query(value = "SELECT * FROM recommend_clothes WHERE user_id = :userId AND weather_id = :weatherId ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
  RecommendClothes findRandomByUserAndWeather(@Param("userId") UUID userId,
      @Param("weather") UUID weatherId);

  ;
}
