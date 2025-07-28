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
        if (item == null || item.getLatitude() == null || item.getLongitude() == null) {
            return null; // chunk에서 제외
        }
        return item;
    }
}
