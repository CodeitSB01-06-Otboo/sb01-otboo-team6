package com.codeit.sb01otbooteam06.domain.clothes.mapper;


import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeDefMapper {

  ClothesAttributeDefDto toDto(AttributeDef attributeDef);
}
