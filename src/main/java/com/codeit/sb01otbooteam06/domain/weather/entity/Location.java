package com.codeit.sb01otbooteam06.domain.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "from")   // 편의용
public class Location {

  private Double latitude;
  private Double longitude;
  private Integer x;
  private Integer y;
}
