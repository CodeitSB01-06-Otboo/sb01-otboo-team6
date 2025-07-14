package com.codeit.sb01otbooteam06.domain.clothes.mapper;

import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeWithDefDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ClothesAttributeWithDefDtoMapper {

  @Mapping(source = "attributeDef.id", target = "definitionId")
  @Mapping(source = "attributeDef.name", target = "definitionName")
  @Mapping(source = "attributeDef.selectableValues", target = "selectableValues")
  ClothesAttributeWithDefDto toDto(ClothesAttribute attribute);


}
