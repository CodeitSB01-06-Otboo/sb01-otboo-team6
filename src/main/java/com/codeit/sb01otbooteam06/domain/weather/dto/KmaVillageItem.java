package com.codeit.sb01otbooteam06.domain.weather.dto;

public record KmaVillageItem(
    String baseDate,    // yyyymmdd
    String baseTime,    // hhmm
    String fcstDate,
    String fcstTime,
    String category,    // TMP, SKY …
    String fcstValue,   // 값(문자/숫자)
    int nx, int ny) {

}
