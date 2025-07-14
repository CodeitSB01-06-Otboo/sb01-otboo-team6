package com.codeit.sb01otbooteam06.domain.clothes.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Getter
@Entity
@Table(name = "recommend_clothes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendClothes extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "weather_id", nullable = false)
  private Weather weather;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "clothes_ids", columnDefinition = "uuid[]", nullable = false)
  private List<UUID> clothesIds;

  public RecommendClothes(Weather weather, User user, List<UUID> clothesIds) {
    this.weather = weather;
    this.user = user;
    this.clothesIds = clothesIds;
  }


}
