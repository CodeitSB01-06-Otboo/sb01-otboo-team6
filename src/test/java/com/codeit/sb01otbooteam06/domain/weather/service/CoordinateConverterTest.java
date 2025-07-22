package com.codeit.sb01otbooteam06.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoordinateConverterTest {

    CoordinateConverter conv = new CoordinateConverter();

    @ParameterizedTest
    @CsvSource({
        "37.5665,126.9780,60,127",   // 서울시청
        "33.4996,126.5312,53,38"     // 제주
    })
    void latLonToGrid(double lat,double lon,int x,int y){
        var grid = conv.latLonToGrid(lat, lon);
        assertThat(grid.gridX()).isEqualTo(x);
        assertThat(grid.gridY()).isEqualTo(y);
    }
}