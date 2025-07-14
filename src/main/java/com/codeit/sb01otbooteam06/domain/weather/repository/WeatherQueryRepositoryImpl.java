package com.codeit.sb01otbooteam06.domain.weather.repository;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.QWeather;
import com.codeit.sb01otbooteam06.domain.weather.entity.QWeatherLocationName;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WeatherQueryRepositoryImpl implements WeatherQueryRepository {

  private final JPAQueryFactory q;
  private static final QWeather w = QWeather.weather;
  private static final QWeatherLocationName ln = QWeatherLocationName.weatherLocationName;
  private final double TOL = 0.0005;
  @Override
  public List<Weather> findForecastBundle(double lat, double lon) {

    /* ── 1단계: 최신 예보 발표 시각 찾기 ───────────────── */
    Instant latest = q.select(w.forecastedAt.max())
        .from(w)
        .where(w.location.latitude.between(lat - TOL, lat + TOL)
            .and(w.location.longitude.between(lon - TOL, lon + TOL)))
        .fetchFirst();

    if (latest == null) return List.of();

    /* ── 2단계: 시간 필터 없이 모두 가져오기 ─────────────── */
    return q.selectFrom(w)
        .leftJoin(w.locationNames, ln).fetchJoin()
        .where(w.location.latitude.between(lat - TOL, lat + TOL)
            .and(w.location.longitude.between(lon - TOL, lon + TOL))
            .and(w.forecastedAt.eq(latest)))
        .orderBy(w.forecastAt.asc())
        .fetch();
  }


  // 2. 위치만 반환
  @Override
  public Optional<Weather> latestWeather(double lat, double lon) {

    Weather weather = q.selectFrom(w)
        .leftJoin(w.locationNames, ln).fetchJoin()    // ← 이름까지 fetch
        .where(w.location.latitude.between(lat - TOL, lat + TOL)
            .and(w.location.longitude.between(lon - TOL, lon + TOL)))
        .orderBy(w.forecastedAt.desc())               // 최신 발표 1건
        .fetchFirst();

    return Optional.ofNullable(weather);
  }

	/*@Override
	public List<Weather> findForecastBundle(double lat, double lon) {
		return List.of();
	}

	@Override
	public Optional<Location> latestLocation(double lat, double lon) {
		return Optional.empty();
	}*/
}