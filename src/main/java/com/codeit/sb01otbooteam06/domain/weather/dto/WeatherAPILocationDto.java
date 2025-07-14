package com.codeit.sb01otbooteam06.domain.weather.dto;

import java.util.List;

public record WeatherAPILocationDto(
    double latitude,
    double longitude,
    int x,
    int y,
    List<String> locationNames
) {

}
