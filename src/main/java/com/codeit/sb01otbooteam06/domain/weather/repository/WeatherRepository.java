package com.codeit.sb01otbooteam06.domain.weather.repository;

import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, UUID>, WeatherQueryRepository {

    /* 자연키 조회 */
    Optional<Weather> findByForecastedAtAndForecastAtAndLocation_XAndLocation_Y(
        Instant forecastedAt, Instant forecastAt, int x, int y);

    Optional<Weather> findByForecastAtAndLocation_XAndLocation_Y(
        Instant forecastAt, int gridX, int gridY);
}

