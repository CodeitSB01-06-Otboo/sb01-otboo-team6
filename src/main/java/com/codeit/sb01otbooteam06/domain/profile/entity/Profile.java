package com.codeit.sb01otbooteam06.domain.profile.entity;

import com.codeit.sb01otbooteam06.domain.base.BaseUpdatableEntity;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseUpdatableEntity {

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id")
  private User user;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Gender gender;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Column(nullable = false)
  private int x;

  @Column(nullable = false)
  private int y;

  @ElementCollection
  @CollectionTable(name = "profile_location_names", joinColumns = @JoinColumn(name = "profile_id"))
  @Column(name = "location_name")
  private List<String> locationNames;

  @Column(nullable = false)
  private int temperatureSensitivity;

  @Column
  private String profileImageUrl;

  public Profile(
          User user,
          String name,
          Gender gender,
          LocalDate birthDate,
          double latitude,
          double longitude,
          int x,
          int y,
          List<String> locationNames,
          int temperatureSensitivity,
          String profileImageUrl
  ) {
    this.user = user;
    this.name = name;
    this.gender = gender;
    this.birthDate = birthDate;
    this.latitude = latitude;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
    this.locationNames = locationNames;
    this.temperatureSensitivity = temperatureSensitivity;
    this.profileImageUrl = profileImageUrl;
  }

  public void update(
          String name,
          Gender gender,
          LocalDate birthDate,
          double latitude,
          double longitude,
          int x,
          int y,
          List<String> locationNames,
          int temperatureSensitivity,
          String profileImageUrl
  ) {
    this.name = name;
    this.gender = gender;
    this.birthDate = birthDate;
    this.latitude = latitude;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
    this.locationNames = locationNames;
    this.temperatureSensitivity = temperatureSensitivity;
    this.profileImageUrl = profileImageUrl;
  }

  /** 프로필 이미지 URL만 업데이트하는 setter */
  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }
}
