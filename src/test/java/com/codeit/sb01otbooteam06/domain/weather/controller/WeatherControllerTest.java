package com.codeit.sb01otbooteam06.domain.weather.controller;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherAPILocationDto;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.service.WeatherService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    WeatherService weatherService;

    @Test
    @DisplayName("GET /api/weathers/location 파라미터 lat,lon 정상 응답")
    void getLocation() throws Exception {
        // given
        WeatherAPILocationDto dto =
            new WeatherAPILocationDto(37, 127, 63, 123,
                List.of("서울", "강남구"));
        given(weatherService.findLocation(anyDouble(), anyDouble()))
            .willReturn(dto);

        // when & then
        mvc.perform(get("/api/weathers/location")
                .param("latitude", "37")
                .param("longitude", "127"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latitude").value(37.0))
            .andExpect(jsonPath("$.locationNames[0]").value("서울"));
    }

    @Test
    @DisplayName("GET /api/weathers 정상 응답")
    void getWeathers() throws Exception {
        // given
        WeatherDto w1 = new WeatherDto(
            UUID.randomUUID(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            new WeatherDto.Loc(37, 127, 63, 123, null),
            SkyStatus.CLEAR,
            new WeatherDto.Precipitation(PrecipitationType.NONE, 0.0, 0.0),
            new WeatherDto.Humidity(50.0, null),
            new WeatherDto.Temperature(25.0, null, 20.0, 30.0),
            new WeatherDto.WindSpeed(3.0, "WEAK"));
        given(weatherService.findLatestBundle(anyDouble(), anyDouble()))
            .willReturn(List.of(w1));

        // when & then
        mvc.perform(get("/api/weathers")
                .param("latitude", "37")
                .param("longitude", "127"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].skyStatus").value("CLEAR"))
            .andExpect(jsonPath("$[0].temperature.current").value(25.0));
    }
}
