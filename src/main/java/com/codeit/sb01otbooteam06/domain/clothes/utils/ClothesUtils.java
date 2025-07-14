package com.codeit.sb01otbooteam06.domain.clothes.utils;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeWithDefDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesAttributeWithDefDtoMapper;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesUtils {

  private final ClothesAttributeWithDefDtoMapper clothesAttributeWithDefDtoMapper;
  private final ClothesMapper clothesMapper;


  /**
   * ClothesDto의 attributes 필드인, List<ClothesAttributeWithDefDto> 를 생성합니다.
   *
   * @param attributes
   * @return List <ClothesAttributeWithDefDto>
   */
  public List<ClothesAttributeWithDefDto> makeClothesAttributeWithDefDtos(
      List<ClothesAttribute> attributes) {
    if (attributes == null) {
      return List.of();
    }
    return attributes.stream()
        .map(clothesAttributeWithDefDtoMapper::toDto)
        .toList();
  }

  /**
   * ClothesDto를 만듭니다
   *
   * @param clothes
   * @param clothesAttributes
   * @return ClothesDto
   */
  public ClothesDto makeClothesDto(Clothes clothes, List<ClothesAttribute> clothesAttributes) {

    ClothesDto clothesDto = clothesMapper.toDto(clothes);
    return new ClothesDto(
        clothesDto.id(),
        clothesDto.ownerId(),
        clothesDto.name(),
        clothesDto.imageUrl(),
        clothesDto.type(),
        makeClothesAttributeWithDefDtos(clothesAttributes) // attribute
    );

  }


}
