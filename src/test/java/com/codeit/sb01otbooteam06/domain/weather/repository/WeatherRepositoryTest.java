package com.codeit.sb01otbooteam06.domain.weather.repository;

import static org.assertj.core.api.BDDAssertions.then;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
})
@Import(WeatherRepositoryTest.QuerydslConfig.class)   // ✅ Querydsl 빈 주입
class WeatherRepositoryTest {

    @TestConfiguration
    static class QuerydslConfig {

        @PersistenceContext
        EntityManager em;

        @Bean
        JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @PersistenceContext
    EntityManager em;

    @Autowired
    WeatherRepository repository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @Transactional
    @Rollback
    @DisplayName("위치로 최신 예보 번들을 조회한다")
    void findForecastBundle() {
        // given ─ 위치
        double lat = 37.0;
        double lon = 127.0;
        Location loc = Location.from(lat, lon, 63, 123);
        Instant now = Instant.now();

        Instant base  = LocalDateTime.of(2025, 7, 20, 20, 0).atZone(KST).toInstant(); // 20일 20:00 KST
        Instant fcst1 = ZonedDateTime.of(2025, 7, 21, 0, 0, 0, 0, KST).toInstant();
        Instant fcst2 = ZonedDateTime.of(2025, 7, 22, 0, 0, 0, 0, KST).toInstant();

        Weather w1 = newWeather(base, fcst1, loc, now);
        w1.applyMetrics(SkyStatus.CLEAR, PrecipitationType.NONE,
            Temperature.from(25.0, 20.0, 30.0),
            null, Wind.from(3.0, 1, 90.0, null, null), 50.0, null, null);

        Weather w2 = newWeather(base, fcst2, loc, now);
        w2.applyMetrics(SkyStatus.CLOUDY, PrecipitationType.RAIN,
            Temperature.from(26.0, 21.0, 31.0),
            null, Wind.from(4.0, 1, 90.0, null, null), 60.0, null, null);

        repository.saveAll(List.of(w1, w2));
        repository.flush();
        em.clear();

        // 🔍 디버깅: 저장된 데이터 확인
        List<Weather> allWeathers = repository.findAll();
        System.out.println("=== 저장된 전체 데이터 ===");
        System.out.println("총 개수: " + allWeathers.size());

        for (Weather w : allWeathers) {
            System.out.printf("ID: %s, lat: %.10f, lon: %.10f, forecastedAt: %s, forecastAt: %s%n",
                w.getId(),
                w.getLocation().getLatitude(),
                w.getLocation().getLongitude(),
                w.getForecastedAt(),
                w.getForecastAt());
        }

        // when
        List<Weather> bundle = repository.findForecastBundle(lat, lon);
        System.out.println("=== 조회 결과 ===");
        System.out.println("조회된 개수: " + bundle.size());

        // then
        then(bundle).hasSize(2);
    }

    @Test
    @Transactional
    @Rollback
    void simpleLocationTest() {
        double lat = 37.0;
        double lon = 127.0;

        Location loc = Location.from(lat, lon, 63, 123);
        System.out.printf("Location 생성: lat=%s, lon=%s%n",
            loc.getLatitude(), loc.getLongitude());

        Weather w = Weather.from(Instant.now(), Instant.now(), loc);
        ReflectionTestUtils.setField(w, "createdAt", Instant.now());
        ReflectionTestUtils.setField(w, "updatedAt", Instant.now());

        repository.save(w);
        repository.flush();

        // 직접 네이티브 쿼리로 확인
        List<Object[]> results = em.createNativeQuery(
                "SELECT lat, lon FROM weathers WHERE lat = 37.0 AND lon = 127.0")
            .getResultList();

        System.out.println("네이티브 쿼리 결과: " + results.size());
        for (Object[] row : results) {
            System.out.printf("저장된 값: lat=%s, lon=%s%n", row[0], row[1]);
        }
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("위치로 최신 Weather 1건을 조회한다")
    void latestWeather() {
        // given
        double lat = 37.0, lon = 127.0;
        Location loc = Location.from(lat, lon, 63, 123);
        Instant now = Instant.now();

        Instant baseOld = Instant.parse("2025-07-20T20:00:00Z");
        Instant baseNew = Instant.parse("2025-07-21T20:00:00Z");

        Instant fcstOld = Instant.parse("2025-07-22T00:00:00Z");
        Instant fcstNew = Instant.parse("2025-07-23T00:00:00Z");

        Weather old = newWeather(baseOld, fcstOld, loc, now);
        Weather neu = newWeather(baseNew, fcstNew, loc, now);

        repository.saveAll(List.of(old, neu));

        // when
        Optional<Weather> latest = repository.latestWeather(lat, lon);

        // then
        then(latest).isPresent();
        then(latest.get().getForecastedAt()).isEqualTo(baseNew);
    }

    private Weather newWeather(Instant base, Instant fcst,
        Location loc, Instant now) {
        Weather w = Weather.from(base, fcst, loc);

        // BaseUpdatableEntity 필드 주입
        ReflectionTestUtils.setField(w, "createdAt", now);
        ReflectionTestUtils.setField(w, "updatedAt", now);

        return w;
    }
}
