package com.codeit.sb01otbooteam06.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageItem;
import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageResponse;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import com.codeit.sb01otbooteam06.domain.weather.exception.WeatherNotFoundException;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    WeatherRepository weatherRepository;
    @Mock KmaApiClient kmaApiClient;
    @Mock KakaoLocalClient kakaoLocalClient;
    @Mock
    CoordinateConverter coordinateConverter;

    WeatherService service;

    @BeforeEach
    void setUp() {
        service = new WeatherService(
            weatherRepository, kmaApiClient,
            kakaoLocalClient, coordinateConverter);
    }

    @Test
    @DisplayName("DB hit 시 Kakao 호출 없이 location 이름을 반환한다")
    void findLocation_dbHit() {
        // given
        double lat=37, lon=127;
        Location loc = Location.from(lat, lon, 63,123);

        Weather w = Weather.from(Instant.now(), Instant.now(), loc);
        w.addLocationName("서울특별시");
        given(weatherRepository.latestWeather(lat, lon))
            .willReturn(Optional.of(w));

        // when
        var dto = service.findLocation(lat, lon);

        // then
        then(dto.locationNames()).containsExactly("서울특별시");
        then(dto.x()).isEqualTo(63);
    }
    @Test
    @DisplayName("findLatestBundle: DB miss → WeatherNotFoundException")
    void latestBundle_notFound() {
        given(weatherRepository.findForecastBundle(37, 127))
            .willReturn(List.of());

        assertThatThrownBy(() -> service.findLatestBundle(37, 127))
            .isInstanceOf(WeatherNotFoundException.class);
    }
    @Test
    @DisplayName("DB miss 시 Kakao·Converter 호출 후 DTO 반환")
    void findLocation_dbMiss() {
        // given
        double lat=37, lon=127;
        given(weatherRepository.latestWeather(lat, lon))
            .willReturn(Optional.empty());
        given(coordinateConverter.latLonToGrid(lat, lon))
            .willReturn(new CoordinateConverter.Grid(63, 123));
        given(kakaoLocalClient.coordToRegion(lat, lon))
            .willReturn(List.of("서울", "강남구"));

        // when
        var dto = service.findLocation(lat, lon);

        // then
        then(dto.locationNames()).containsExactly("서울", "강남구");
        then(dto.x()).isEqualTo(63);
    }

    @Test
    @DisplayName("KMA → DailyAggregator → Weather 저장까지 수행한다")
    void saveVillageForecast() {
        // given
        double lat=37, lon=127;
        CoordinateConverter.Grid grid = new CoordinateConverter.Grid(63,123);
        given(coordinateConverter.latLonToGrid(lat, lon)).willReturn(grid);

        // KMA 응답 (TMP 2건만 넣어 단순화)
        LocalDate fcstDate = LocalDate.now();
        List<KmaVillageItem> items = List.of(
            new KmaVillageItem("20250720","0500",
                fcstDate.toString().replace("-",""),
                "0300","TMP","24",63,123),
            new KmaVillageItem("20250720","0500",
                fcstDate.toString().replace("-",""),
                "0600","TMP","26",63,123)
        );
        given(kmaApiClient.getVillageFcst(grid.gridX(), grid.gridY()))
            .willReturn(new KmaVillageResponse(items));

        given(weatherRepository.findByForecastAtAndLocation_XAndLocation_Y(any(), anyInt(), anyInt()))
            .willReturn(Optional.empty());
        given(kakaoLocalClient.coordToRegion(lat, lon))
            .willReturn(List.of("서울","강남구"));

        // when
        service.saveVillageForecast(lat, lon);

        // then
        thenCode(() ->
            verify(weatherRepository, atLeastOnce()).save(any(Weather.class))
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("findLatestBundle: min/max·전일대비·보퍼트 레이블까지 DTO 변환")
    void latestBundle_success() {
        // ── given ──
        double lat = 37, lon = 127;
        Location loc = Location.from(lat, lon, 63, 123);

        Instant base = Instant.parse("2025-07-20T11:00:00Z");
        Instant d0   = Instant.parse("2025-07-21T15:00:00Z"); // 오늘
        Instant d1   = Instant.parse("2025-07-22T15:00:00Z"); // 내일

        Weather today  = Weather.from(base, d0, loc);
        Weather tomorr = Weather.from(base, d1, loc);

        // 오늘: current=26, min/max *없음*
        today.applyMetrics(SkyStatus.CLEAR, PrecipitationType.NONE,
            Temperature.from(26.0, null, null),
            null, Wind.from(3.2, null, null, null, null),
            60.0, null, null);

        // 내일: current=28, min/max 존재
        tomorr.applyMetrics(SkyStatus.CLOUDY, PrecipitationType.RAIN,
            Temperature.from(28.0, 22.0, 32.0),
            Precipitation.from(5.0, null, 60.0),
            Wind.from(9.5, null, null, null, null),
            70.0, null, null);

        given(weatherRepository.findForecastBundle(lat, lon))
            .willReturn(List.of(today, tomorr));

        // ── when ──
        List<WeatherDto> list = service.findLatestBundle(lat, lon);

        // ── then ──
        // 전체 2건
        assertThat(list).hasSize(2);

        WeatherDto t0 = list.get(0);
        WeatherDto t1 = list.get(1);

        // (1) min/max 보간: today 도 null → 내일 값(22·32)으로 채워져야 함
        assertThat(t0.temperature().min()).isEqualTo(22.0);
        assertThat(t0.temperature().max()).isEqualTo(32.0);

        // (2) 전일 대비 current ΔT / ΔHumidity
        assertThat(t1.temperature().comparedToDayBefore()).isEqualTo(28.0 - 26.0); // +2
        assertThat(t1.humidity().comparedToDayBefore()).isEqualTo(70.0 - 60.0);    // +10

        // (3) Wind speed 레이블 (3.2 m/s → WEAK, 9.5 m/s → STRONG)
        assertThat(t0.windSpeed().asWord()).isEqualTo("WEAK");
        assertThat(t1.windSpeed().asWord()).isEqualTo("STRONG");

        // (4) 강수 없을 때 amount/probability 0 으로 매핑
        assertThat(t0.precipitation().amount()).isZero();
        assertThat(t0.precipitation().probability()).isZero();

        // (5) Location 변환
        assertThat(t0.location().x()).isEqualTo(63);
        assertThat(t0.location().y()).isEqualTo(123);
    }
}
