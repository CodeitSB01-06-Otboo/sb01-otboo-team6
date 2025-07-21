package com.codeit.sb01otbooteam06.domain.weather.dto;

import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;

public record PrecipitationDto(
    PrecipitationType type,
    double amount,
    double probability
) {
}
