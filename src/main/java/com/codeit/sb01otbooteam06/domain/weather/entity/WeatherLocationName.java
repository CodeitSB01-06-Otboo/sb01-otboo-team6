package com.codeit.sb01otbooteam06.domain.weather.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "weather_location_names",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"weather_id", "location_name"})
)
public class WeatherLocationName extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_id", nullable = false)
    private Weather weather;

    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;

    private WeatherLocationName(Weather w, String name) {
        this.weather = w;
        this.locationName = name;
    }

    public static WeatherLocationName from(Weather w, String name) {
        return new WeatherLocationName(w, name);
    }
}
