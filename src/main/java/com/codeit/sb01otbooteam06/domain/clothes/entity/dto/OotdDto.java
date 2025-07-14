package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;
import java.util.UUID;

//추천시 반환하는 의상 정보 dto
public record OotdDto(
    UUID clothesId,
    String name,
    String imageUrl,
    String type,
    List<ClothesAttributeWithDefDto> attributes
) {

  public static OotdDto toDto(ClothesDto clothesDto) {
    return new OotdDto(
        clothesDto.id(),
        clothesDto.name(),
        clothesDto.imageUrl(),
        clothesDto.type(),
        clothesDto.attributes()
    );
  }
}
