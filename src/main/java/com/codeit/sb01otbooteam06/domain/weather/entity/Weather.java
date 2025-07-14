package com.codeit.sb01otbooteam06.domain.weather.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
    name = "weathers",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"forecast_at", "grid_x", "grid_y"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Weather extends BaseUpdatableEntity {

    /* ── 시각 ─────────────────────── */
    @Column(name = "forecasted_at", nullable = false)
    private Instant forecastedAt;    // 발표(baseDate+baseTime)

    @Column(name = "forecast_at", nullable = false)
    private Instant forecastAt;      // 예보(fcstDate+fcstTime)

    /* ── 좌표 ─────────────────────── */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "lat")),
        @AttributeOverride(name = "longitude", column = @Column(name = "lon")),
        @AttributeOverride(name = "x", column = @Column(name = "grid_x")),
        @AttributeOverride(name = "y", column = @Column(name = "grid_y"))
    })
    private Location location;

    /* ── 상태 코드 ────────────────── */
    @Enumerated(EnumType.STRING)
    @Column(name = "sky_status", length = 12)
    private SkyStatus skyStatus;           // SKY

    @Enumerated(EnumType.STRING)
    @Column(name = "precipitation_type", length = 15)
    private PrecipitationType precipitationType;          // PTY

    /* ── 값 객체(Embedded) ─────────── */
    @Embedded
    private Temperature temperature;     // current / min / max

    @Embedded
    private Precipitation precipitation; // amount / amountText / probability

    @Embedded
    private Wind wind;                   // speed / level / direction / u / v

    @Column(name = "humidity")
    private Double humidity;             // REH
    @Column(name = "snow_amount")
    private Double snowAmount;           // SNO
    @Column(name = "lightning")
    private Double lightning;            // LGT

    /* ── 위치 이름 목록 ────────────── */
    @OneToMany(mappedBy = "weather",
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    private List<WeatherLocationName> locationNames = new ArrayList<>();

    /* ===== 생성 ===== */
    private Weather(Instant base, Instant fcst, Location loc) {
        this.forecastedAt = base;
        this.forecastAt = fcst;
        this.location = loc;
    }

    public static Weather from(Instant base, Instant fcst, Location loc) {
        return new Weather(base, fcst, loc);
    }

    /* ===== 편의 ===== */
    public void addLocationName(String name) {
        locationNames.add(WeatherLocationName.from(this, name));
    }
    /**
     * 더 최신 발표 시각이 들어오면 교체합니다.
     */
    public void refreshBaseIfNewer(Instant newerBase) {
        if (newerBase.isAfter(this.forecastedAt)) {
            this.forecastedAt = newerBase;
        }
    }
    public void applyMetrics(SkyStatus skyStatus,
        PrecipitationType ptyType,
        Temperature temp,
        Precipitation precip,
        Wind wind,
        Double humidity,
        Double snow,
        Double lightning) {
        this.skyStatus = skyStatus;
        this.precipitationType = ptyType;
        this.temperature = temp;
        this.precipitation = precip;
        this.wind = wind;
        this.humidity = humidity;
        this.snowAmount = snow;
        this.lightning = lightning;
    }
}
