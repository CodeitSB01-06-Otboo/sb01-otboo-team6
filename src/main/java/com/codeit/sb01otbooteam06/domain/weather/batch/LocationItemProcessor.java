package com.codeit.sb01otbooteam06.domain.weather.batch;


import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class LocationItemProcessor
    implements ItemProcessor<Location, Location> {

    @Override
    public Location process(Location item) {
        return item;   // 그대로 Writer 로 전달
    }
}
