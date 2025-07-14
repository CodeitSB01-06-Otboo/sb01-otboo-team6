package com.codeit.sb01otbooteam06.domain.weather.mapper;

import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.WeatherLocationName;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WeatherDtoMapper {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  // Weather → WeatherDto
  public WeatherDto toDto(Weather w) {
    if (w == null) {
      return null;
    }

    return new WeatherDto(
        w.getId(),
        toKst(w.getForecastedAt()),
        toKst(w.getForecastAt()),
        toLoc(w),
        w.getSkyStatus(),
        toPrecip(w),
        new WeatherDto.Humidity(w.getHumidity(), null),
        toTemp(w.getTemperature()),
        toWindSpeed(w.getWind())
    );
  }

  // 매핑 메서드
  private WeatherDto.Loc toLoc(Weather w) {
    Location loc = w.getLocation();
    List<String> names = w.getLocationNames()
        .stream()
        .map(WeatherLocationName::getLocationName)
        .toList();

    return new WeatherDto.Loc(loc.getLatitude(), loc.getLongitude(), loc.getX(), loc.getY(),
        names);
  }

  private WeatherDto.Precipitation toPrecip(Weather w) {
    var p = w.getPrecipitation();
    return new WeatherDto.Precipitation(w.getPrecipitationType(),
        p == null ? null : p.getAmount(), p == null ? null : p.getProbability()
    );
  }

  private WeatherDto.Temperature toTemp(Temperature t) {
    return t == null ? null
        : new WeatherDto.Temperature(t.getCurrent(), null, t.getMin(), t.getMax());
  }

  private WeatherDto.WindSpeed toWindSpeed(Wind w) {
    return w == null ? null
        : new WeatherDto.WindSpeed(w.getSpeed(), beaufortToWord(w.getLevel()));
  }

  private LocalDateTime toKst(Instant i) {
    return i == null ? null : LocalDateTime.ofInstant(i, KST);
  }

  private String beaufortToWord(Integer level) {
    return level == null ? null : switch (level) {
      case 0 -> "평온";
      case 1 -> "연풍";
      case 2 -> "남실바람";
      case 3 -> "산들바람";
      case 4 -> "건들바람";
      case 5 -> "흔들바람";
      case 6 -> "된바람";
      case 7 -> "센바람";
      case 8 -> "큰바람";
      case 9 -> "돌풍";
      case 10 -> "폭풍";
      case 11 -> "강풍";
      case 12 -> "태풍";
      default -> "알 수 없음";
    };
  }
}