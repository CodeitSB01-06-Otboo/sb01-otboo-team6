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
    @AttributeOverride(name = "amount", column = @Column(name = "precipitation_amount")),
    @AttributeOverride(name = "amountText", column = @Column(name = "precipitation_amount_text")),
    @AttributeOverride(name = "probability", column = @Column(name = "precipitation_probability"))
})
public class Precipitation {

    private Double amount;      // PCP
    private String amountText; // "1 mm 미만"
    private Double probability; // POP
}
