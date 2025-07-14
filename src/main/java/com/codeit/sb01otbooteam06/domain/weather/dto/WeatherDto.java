package com.codeit.sb01otbooteam06.domain.weather.dto;

import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WeatherDto(
    UUID id,
    LocalDateTime forecastedAt,
    LocalDateTime forecastAt,
    Loc location,
    SkyStatus skyStatus,
    Precipitation precipitation,
    Humidity humidity,
    Temperature temperature,
    WindSpeed windSpeed
) {
    public record Loc(double latitude, double longitude, int x, int y, List<String> locationNames){}
    public record Precipitation(PrecipitationType type, double amount, double probability){}
    public record Humidity(double current,Double comparedToDayBefore){}
    public record Temperature(double current,Double comparedToDayBefore,
                              Double min,Double max){}
    public record WindSpeed(double speed,String asWord){}
}
