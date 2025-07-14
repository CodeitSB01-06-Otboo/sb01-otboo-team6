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
    @AttributeOverride(name = "current", column = @Column(name = "temperature_current")),
    @AttributeOverride(name = "min", column = @Column(name = "temperature_min")),
    @AttributeOverride(name = "max", column = @Column(name = "temperature_max"))
})
public class Temperature {

    private Double current;   // TMP
    private Double min;       // TMN
    private Double max;       // TMX
}
