package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.RecommendClothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendClothesRepository extends JpaRepository<RecommendClothes, UUID> {

  boolean existsByUserAndWeather(User user, Weather weather);

  void deleteByUserAndWeather(User user, Weather weather);

}
