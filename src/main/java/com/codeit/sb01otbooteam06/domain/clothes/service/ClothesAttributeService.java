package com.codeit.sb01otbooteam06.domain.clothes.service;


import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDto;
import com.codeit.sb01otbooteam06.domain.clothes.exception.AttributeDefNotFoundException;
import com.codeit.sb01otbooteam06.domain.clothes.repository.AttributeDefRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesAttributeService {

  /// 의상-속성 중간 테이블 서비스

  private final ClothesAttributeRepository clothesAttributeRepository;
  private final AttributeDefRepository attributeDefRepository;

  /**
   * 의상-속성 중간 테이블에 의상에 대한 속성을 저장합니다.
   *
   * @param clothes
   * @param attributes
   * @return 의상에 대한 ClothesAttribute 객체 리스트
   */
  @Transactional
  public List<ClothesAttribute> create(Clothes clothes, List<ClothesAttributeDto> attributes) {
    List<ClothesAttribute> savedAttributes = new ArrayList<>();

    //todo: value가 속성리스트에 포함되어있는지 예외체크

    List<ClothesAttribute> clothesAttributes = attributes.stream()
        .map(attribute -> {
          AttributeDef targetAttributeDef = attributeDefRepository.findById(
              UUID.fromString(attribute.definitionId())
          ).orElseThrow(AttributeDefNotFoundException::new);

          return new ClothesAttribute(
              clothes,
              targetAttributeDef,
              attribute.value()
          );
        })
        .toList();

    return clothesAttributeRepository.saveAll(clothesAttributes);

  }

  @Transactional
  public List<ClothesAttribute> update(Clothes clothes, List<ClothesAttributeDto> attributes) {

    //의상에 대한 기존 중간테이블 삭제 후 생성
    clothesAttributeRepository.deleteByClothes(clothes);
    return this.create(clothes, attributes);

  }
}
