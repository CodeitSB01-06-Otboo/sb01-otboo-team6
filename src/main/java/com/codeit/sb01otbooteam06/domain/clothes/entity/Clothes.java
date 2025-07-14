package com.codeit.sb01otbooteam06.domain.clothes.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "clothes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Clothes extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "imageUrl", nullable = true)
  private String imageUrl;

  public Clothes(User owner, String name, String type, String imageUrl) {
    this.owner = owner;
    this.name = name;
    this.type = type;
    this.imageUrl = imageUrl;

  }

  public void update(String name, String type, String imageUrl
  ) {
    this.name = name;
    this.type = type;
    this.imageUrl = imageUrl;
  }
}
