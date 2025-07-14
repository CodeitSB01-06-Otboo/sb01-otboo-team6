package com.codeit.sb01otbooteam06.domain.clothes.mapper;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClothesMapper {


  @Mapping(source = "owner.id", target = "ownerId")
  @Mapping(target = "attributes", ignore = true)
    // ignore = true는 해당 필드 매핑을 무시함. 추후 서비스에서 별도 주입
  ClothesDto toDto(Clothes clothes);


  Clothes toEntity(ClothesDto clothesDto);

}
