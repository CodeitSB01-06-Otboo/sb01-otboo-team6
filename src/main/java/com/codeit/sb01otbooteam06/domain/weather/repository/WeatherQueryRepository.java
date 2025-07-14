package com.codeit.sb01otbooteam06.domain.weather.repository;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.List;
import java.util.Optional;

public interface WeatherQueryRepository {

  /**
   * 같은 위·경도(lat, lon) 에서 ① 가장 최신 forecastedAt 값을 구하고 ② 그 시점에 생성된 forecastAt 행 전체를 반환
   */
  List<Weather> findForecastBundle(double lat, double lon);

  // 위치 정보만 필요할 시 반환
  Optional<Weather> latestWeather(double lat, double lon);

}
