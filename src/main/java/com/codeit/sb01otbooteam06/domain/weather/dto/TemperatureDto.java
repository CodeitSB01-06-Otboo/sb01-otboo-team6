package com.codeit.sb01otbooteam06.domain.weather.dto;

public record TemperatureDto(
    double current,
    //전날 대비 온도 변동이 필요 한가..?
    double comparedToDayBefore,
    double min,
    double max
) {

}
