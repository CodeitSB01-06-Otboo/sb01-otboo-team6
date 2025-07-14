package com.codeit.sb01otbooteam06.domain.weather.batch;

import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.service.WeatherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class WeatherItemWriter implements ItemWriter<Location> {

    private final WeatherService weatherService;

    @Override
    public void write(Chunk<? extends Location> chunk) {
        for (Location loc : chunk) {
            try {
                weatherService.saveVillageForecast(loc.getLatitude(), loc.getLongitude());
            } catch (Exception ex) {
                log.warn("Failed at {},{}  – {}", loc.getLatitude(), loc.getLongitude(), ex.getMessage());
                throw ex;   // Batch retry/skip 정책에 맡김
            }
        }
    }
}
