package com.codeit.sb01otbooteam06.domain.weather.batch;

import com.codeit.sb01otbooteam06.domain.profile.repository.ProfileRepository;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import jakarta.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LocationItemReader implements ItemReader<Location> {

    private final ProfileRepository profileRepository;
    private Iterator<Location> cursor;

    @PostConstruct
    void init() {
        List<Location> list = profileRepository.findDistinctLocations();
        cursor = list.iterator();
        log.info("LocationReader init : {} distinct locations", list.size());
    }

    @Override
    public Location read() {
        return (cursor != null && cursor.hasNext()) ? cursor.next() : null;
    }
}
