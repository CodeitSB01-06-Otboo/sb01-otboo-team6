package com.codeit.sb01otbooteam06.domain.clothes.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "attributes_defs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeDef extends BaseUpdatableEntity {

  //의상속성
  @Column(name = "name", nullable = false)
  private String name;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "selectable_values", columnDefinition = "text[]", nullable = false)
  private List<String> selectableValues;


  public AttributeDef(String name, List<String> selectableValues) {
    this.name = name;
    this.selectableValues = selectableValues;
  }

  public void update(String name, List<String> selectableValues) {
    this.name = name;
    this.selectableValues = selectableValues;
  }
}
