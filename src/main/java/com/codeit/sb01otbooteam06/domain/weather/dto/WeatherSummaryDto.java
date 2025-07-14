package com.codeit.sb01otbooteam06.domain.weather.dto;

import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import java.util.UUID;

public record WeatherSummaryDto(
    UUID weatherId,
    SkyStatus skyStatus,
    PrecipitationDto precipitation,
    TemperatureDto temperature
) {

}
