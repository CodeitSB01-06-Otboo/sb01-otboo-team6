package com.codeit.sb01otbooteam06.domain.weather.service;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.codeit.sb01otbooteam06.domain.weather.dto.ForecastKey;
import com.codeit.sb01otbooteam06.domain.weather.dto.ForecastKeyDaily;
import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageResponse;
import com.codeit.sb01otbooteam06.domain.weather.dto.KmaVillageItem;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherAPILocationDto;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherMetrics;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.WeatherLocationName;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import com.codeit.sb01otbooteam06.domain.weather.exception.WeatherNotFoundException;
import com.codeit.sb01otbooteam06.domain.weather.mapper.WeatherMapper;
import com.codeit.sb01otbooteam06.domain.weather.repository.WeatherRepository;
import com.codeit.sb01otbooteam06.domain.weather.util.DailyAggregator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final KmaApiClient kmaApiClient;
    private final KakaoLocalClient kakaoLocalClient;
    private final CoordinateConverter coordinateConverter;

    /* 위치 조회 */
    public WeatherAPILocationDto findLocation(double lat, double lon) {

        // DB에서 해당 위치의 이름명이 있는지 확인
        return weatherRepository.latestWeather(lat, lon)
            .map(weather -> {
                // 행정동 이름: DB에 있으면 그대로, 없으면 Kakao 호출
                List<String> names = weather.getLocationNames().isEmpty()
                    ? kakaoLocalClient.coordToRegion(lat, lon)
                    : weather.getLocationNames()
                        .stream()
                        .map(WeatherLocationName::getLocationName)
                        .toList();

                // Location 임베디드 꺼내서 DTO 변환
                return toLocationRes(weather.getLocation(), names);
            })
            // DB miss → 좌표 변환 + Kakao 호출 (기존 로직 동일)
            .orElseGet(() -> {
                CoordinateConverter.Grid grid = coordinateConverter.latLonToGrid(lat, lon);
                List<String> names = kakaoLocalClient.coordToRegion(lat, lon);
                return new WeatherAPILocationDto(lat, lon, grid.gridX(), grid.gridY(), names);
            });
    }

    /* -------------- 최신 예보 번들 -------------- */
    public List<WeatherDto> findLatestBundle(double lat, double lon) {

        List<Weather> bundle = weatherRepository.findForecastBundle(lat, lon);
        if (bundle.isEmpty()) {
            throw new WeatherNotFoundException().withLatLon(lat, lon);
        }

        fillMinMax(bundle);   // ← min/max null 보간

        /* 전일 대비용 맵 (전일 같은 forecastAt) */
        Map<Instant, Weather> yMap = bundle.stream()
            .collect(Collectors.toMap(Weather::getForecastAt, w -> w));

        return bundle.stream()
            .map(w -> toWeatherRes(w,
                yMap.get(w.getForecastAt().minus(1, DAYS))))
            .toList();
    }

    /**
     * 단기예보 “getVilageFcst”를 호출해 Weather 엔티티를 저장한다.
     *
     * @param lat 위도
     * @param lon 경도
     */
    @Transactional
    public void saveVillageForecast(double lat, double lon) {

        /* 1) 좌표 변환 */
        CoordinateConverter.Grid grid = coordinateConverter.latLonToGrid(lat, lon);

        /* 2) 단기예보 호출 */
        KmaVillageResponse dto = kmaApiClient.getVillageFcst(grid.gridX(), grid.gridY());
        if (dto.items().isEmpty()) {
            return;
        }

        /* 3) 발표‧예보 시각 계산 */
        String baseDate = dto.items().get(0).baseDate();   // yyyymmdd
        String baseTime = dto.items().get(0).baseTime();   // HHmm(예: 0500)
        ZoneId  KST      = ZoneId.of("Asia/Seoul");

        Instant baseInst = LocalDateTime
            .parse(baseDate + String.format("%04d", Integer.parseInt(baseTime)),
                DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
            .atZone(KST).toInstant();

        /* 4) fcstDate(하루 단위)로 그룹핑 → 집계 → UPSERT */
        dto.items().stream()
            .collect(Collectors.groupingBy(i ->
                LocalDate.parse(i.fcstDate(), DateTimeFormatter.BASIC_ISO_DATE)))
            .forEach((fcstDate, list) -> {

                /* 4-1) 일별 통계값 */
                DailyAggregator.DailyAgg a = DailyAggregator.aggregate(list);

                Instant fcstInst = fcstDate.atStartOfDay(KST).toInstant();   // 00:00 KST

                /* 4-2) 자연키로 먼저 찾아본다 */
                Weather w = weatherRepository
                    .findByForecastAtAndLocation_XAndLocation_Y(
                        fcstInst, grid.gridX(), grid.gridY())
                    .orElseGet(() -> Weather.from(
                        baseInst, fcstInst,
                        Location.from(lat, lon, grid.gridX(), grid.gridY())));

                // 발표 시각이 더 최신이면 덮어쓰기
                if (baseInst.isAfter(w.getForecastedAt())) {
                    w.refreshBaseIfNewer(baseInst);
                }
                /* 4-3) 메트릭 갱신 */
                w.applyMetrics(
                    a.sky(), a.pty(),
                    Temperature.from(a.tmpAvg(), a.tmpMin(), a.tmpMax()),
                    Precipitation.from(a.pcpSum(), null, a.popMax()),
                    Wind.from(a.wsdAvg(), null, null, null, null),
                    a.rehAvg(),
                    null,
                    null);

                /* 4-4) 행정동 명칭은 한 번만 저장 */
                if (w.getLocationNames().isEmpty()) {
                    kakaoLocalClient.coordToRegion(lat, lon).forEach(name -> {
                        if (w.getLocationNames().stream()
                            .noneMatch(n -> n.getLocationName().equals(name))) {
                            w.addLocationName(name);
                        }
                    });
                }

                /* 4-5) INSERT 또는 UPDATE */
                weatherRepository.save(w);
            });
    }

    /* private  */
    private static WeatherAPILocationDto toLocationRes(
        Location loc, List<String> names) {
        return new WeatherAPILocationDto(
            loc.getLatitude(), loc.getLongitude(),
            loc.getX(), loc.getY(), names);
    }

    private static WeatherDto toWeatherRes(Weather w, Weather yDay) {

        double curT = ofNullable(w.getTemperature().getCurrent()).orElse(0.0);
        Double yT = yDay == null ? null : yDay.getTemperature().getCurrent();

        double curH = ofNullable(w.getHumidity()).orElse(0.0);
        Double yH = yDay == null ? null : yDay.getHumidity();

        WeatherDto.Temperature tDto = new WeatherDto.Temperature(
            curT, yT == null ? null : curT - yT,
            w.getTemperature().getMin(),
            w.getTemperature().getMax());

        WeatherDto.Humidity hDto = new WeatherDto.Humidity(
            curH, yH == null ? null : curH - yH);

        WeatherDto.Precipitation pDto = toPrecip(w);

        double spd = ofNullable(w.getWind().getSpeed()).orElse(0.0);
        String word = spd < 4 ? "WEAK" : spd < 9 ? "MODERATE" : "STRONG";
        WeatherDto.WindSpeed wsDto = new WeatherDto.WindSpeed(spd, word);

        Location l = w.getLocation();
        WeatherDto.Loc locDto = new WeatherDto.Loc(
            l.getLatitude(), l.getLongitude(),
            l.getX(), l.getY(), null);

        return new WeatherDto(
            w.getId(),
            w.getForecastedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            w.getForecastAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
            locDto, w.getSkyStatus(),
            pDto, hDto, tDto, wsDto);
    }

    private static WeatherDto.Precipitation toPrecip(Weather w) {
        if (w.getPrecipitation() == null) {
            return new WeatherDto.Precipitation(
                w.getPrecipitationType(), 0.0, 0.0);
        }
        return new WeatherDto.Precipitation(
            w.getPrecipitationType(),
            ofNullable(w.getPrecipitation().getAmount()).orElse(0.0),
            ofNullable(w.getPrecipitation().getProbability()).orElse(0.0));
    }

    /* ---------- min/max 채우기 ---------- */
    private static void fillMinMax(List<Weather> bundle) {

        Double min = bundle.stream()
            .map(w -> w.getTemperature().getMin())
            .filter(Objects::nonNull)
            .min(Double::compare).orElse(null);

        Double max = bundle.stream()
            .map(w -> w.getTemperature().getMax())
            .filter(Objects::nonNull)
            .max(Double::compare).orElse(null);

        if (min == null && max == null) {
            return; // 둘 다 없으면 끝
        }

        bundle.forEach(w -> {
            Temperature old = w.getTemperature();
            Temperature filled = Temperature.from(
                old.getCurrent(),      // current 유지
                min,                   // 공통 min
                max);                  // 공통 max
            w.applyMetrics(
                w.getSkyStatus(),
                w.getPrecipitationType(),
                filled,
                w.getPrecipitation(),
                w.getWind(),
                w.getHumidity(),
                w.getSnowAmount(),
                w.getLightning());
        });
    }
}
