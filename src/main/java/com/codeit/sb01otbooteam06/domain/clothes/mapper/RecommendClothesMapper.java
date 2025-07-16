package com.codeit.sb01otbooteam06.domain.clothes.mapper;

import com.codeit.sb01otbooteam06.domain.clothes.entity.RecommendClothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {Collections.class})
public interface RecommendClothesMapper {

  @Mapping(target = "weather", source = "weather")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "clothesIds", source = "clothesIds")
  RecommendClothes toEntity(Weather weather, User user, List<UUID> clothesIds);
}
