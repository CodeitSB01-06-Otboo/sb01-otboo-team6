package com.codeit.sb01otbooteam06.domain.clothes.mapper;


import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClothesAttributeMapper {


  ClothesAttributeDto toDto(ClothesAttribute clothesAttribute);


}
