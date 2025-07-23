package com.codeit.sb01otbooteam06.util;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class EntityProvider {


  public static User createTestUser() {
    return User.builder()
        .email("test@example.com")
        .password("securePassword123!")
        .name("테스트유저")
        .role(Role.USER)
        .linkedOAuthProviders(List.of())
        .locked(false)
        .build();
  }

  public static Profile createTestProfile(User user) {
    return new Profile(
        user,
        "테스트프로필",
        Gender.MALE,
        LocalDate.of(1995, 5, 20),
        37.5665,   // latitude (예: 서울 좌표)
        126.9780,  // longitude
        60,        // x
        127,       // y
        List.of("서울특별시", "중구"),
        5,         // temperatureSensitivity
        "profile.url"
    );
  }

  public static Clothes createTestClothes(User user) {
    return new Clothes(
        user, "상의", "TOP", "image.url"
    );
  }

  public static AttributeDef createTestAttributeDef(String name, List<String> values) {
    return new AttributeDef(name, values);
  }

  public static ClothesAttribute createTestClothesAttribute(Clothes clothes,
      AttributeDef attributeDef, String value
  ) {
    return new ClothesAttribute(clothes, attributeDef, value);
  }

  public static Weather createTestWeather(Location location, SkyStatus skyStatus,
      PrecipitationType type, Temperature temperature, Precipitation precipitation, Wind wind,
      double humidity, double snow, double lighting) {

    Weather weather = Weather.from(
        Instant.now().minusSeconds(600),
        Instant.now().plusSeconds(3600),
        location
    );
    weather.applyMetrics(
        skyStatus,
        type,
        temperature,
        precipitation,
        wind,
        humidity,
        snow,
        lighting
    );
    return weather;
  }


}
