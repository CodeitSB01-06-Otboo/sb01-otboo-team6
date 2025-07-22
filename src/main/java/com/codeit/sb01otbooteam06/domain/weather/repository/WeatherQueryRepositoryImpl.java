package com.codeit.sb01otbooteam06.domain.weather.repository;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.QWeather;
import com.codeit.sb01otbooteam06.domain.weather.entity.QWeatherLocationName;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class WeatherQueryRepositoryImpl implements WeatherQueryRepository {

  private final JPAQueryFactory q;
  private static final QWeather w = QWeather.weather;
  private static final QWeatherLocationName ln = QWeatherLocationName.weatherLocationName;
  private final double TOL = 0.001;

  @Override
  public List<Weather> findForecastBundle(double lat, double lon) {
    System.out.printf("🔍 findForecastBundle 호출: lat=%.10f, lon=%.10f%n", lat, lon);
    System.out.printf("🔍 검색 범위: lat=[%.10f ~ %.10f], lon=[%.10f ~ %.10f]%n",
        lat - TOL, lat + TOL, lon - TOL, lon + TOL);

    double latMin = lat - TOL, latMax = lat + TOL;
    double lonMin = lon - TOL, lonMax = lon + TOL;

    //최신 예보 발표 시각 찾기
    Instant latest = q.select(w.forecastedAt.max())
        .from(w)
        .where(w.location.latitude.between(latMin, latMax)
            .and(w.location.longitude.between(lonMin, lonMax)))
        .fetchFirst();

    System.out.println("🔍 찾은 최신 forecastedAt: " + latest);

    if (latest == null) {
      System.out.println("🔍 latest가 null이므로 빈 리스트 반환");
      return List.of();
    }

    /* ── 2) 실제 조회: 서브쿼리로 EQ 비교 (JDBC 바인딩 문제 회피) ─────────── */
    QWeather sub = new QWeather("sub");

    List<Weather> result = q.selectFrom(w)
        .leftJoin(w.locationNames, ln).fetchJoin()
        .where(
            w.location.latitude.between(latMin, latMax),
            w.location.longitude.between(lonMin, lonMax),
            w.forecastedAt.eq(
                JPAExpressions.select(sub.forecastedAt.max())
                    .from(sub)
                    .where(
                        sub.location.latitude.between(latMin, latMax),
                        sub.location.longitude.between(lonMin, lonMax)
                    )
            )
        )
        .orderBy(w.forecastAt.asc())
        .fetch();

    System.out.println("🔍 최종 결과 개수: " + result.size());
    return result;
  }

  // 2. 위치만 반환
  @Override
  public Optional<Weather> latestWeather(double lat, double lon) {

    Weather weather = q.selectFrom(w)
        .leftJoin(w.locationNames, ln).fetchJoin()
        .where(w.location.latitude.between(lat - TOL, lat + TOL)
            .and(w.location.longitude.between(lon - TOL, lon + TOL)))
        .orderBy(w.forecastedAt.desc())
        .fetchFirst();

    return Optional.ofNullable(weather);
  }

}