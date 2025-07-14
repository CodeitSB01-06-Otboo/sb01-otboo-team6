package com.codeit.sb01otbooteam06.domain.weather.dto;

import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;

/**
 * 한 번의 예보(row 묶음)를 Weather.applyMetrics()에 넣기 위한 value-object.
 */
public record WeatherMetrics(
    SkyStatus skyStatus,
    PrecipitationType ptyType,
    Temperature temperature,
    Precipitation precipitation,
    Wind wind,
    Double humidity,
    Double snow,
    Double lightning) {

}
