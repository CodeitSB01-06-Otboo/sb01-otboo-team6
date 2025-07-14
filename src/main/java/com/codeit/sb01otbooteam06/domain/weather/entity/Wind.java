package com.codeit.sb01otbooteam06.domain.weather.entity;


import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "from")
@AttributeOverrides({
    @AttributeOverride(name = "speed", column = @Column(name = "wind_speed")),
    @AttributeOverride(name = "level", column = @Column(name = "wind_level")),
    @AttributeOverride(name = "direction", column = @Column(name = "wind_direction")),
    @AttributeOverride(name = "u", column = @Column(name = "wind_u")),
    @AttributeOverride(name = "v", column = @Column(name = "wind_v"))
})
public class Wind {

    private Double speed;     // WSD
    private Integer level;    // 1·2·3
    private Double direction; // VEC
    private Double u;         // UUU
    private Double v;         // VVV
}
