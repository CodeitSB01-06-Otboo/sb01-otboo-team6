package com.codeit.sb01otbooteam06.domain.weather.controller;

import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherAPILocationDto;
import com.codeit.sb01otbooteam06.domain.weather.dto.WeatherDto;
import com.codeit.sb01otbooteam06.domain.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/location")
    public WeatherAPILocationDto getLocation(@RequestParam double latitude,
        @RequestParam double longitude) {
        return weatherService.findLocation(latitude, longitude);
    }

    @GetMapping
    public List<WeatherDto> getWeathers(@RequestParam double latitude,
        @RequestParam double longitude) {
        return weatherService.findLatestBundle(latitude, longitude);
    }
}
