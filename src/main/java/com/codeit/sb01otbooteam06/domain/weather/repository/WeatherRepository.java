package com.codeit.sb01otbooteam06.domain.weather.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, UUID>, WeatherQueryRepository {

}

