package com.codeit.sb01otbooteam06.domain.clothes.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.exception.AttributeDefAlreadyExistsException;
import com.codeit.sb01otbooteam06.domain.clothes.exception.AttributeDefNotFoundException;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.AttributeDefMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.AttributeDefRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeDefService {

  /// 의상 속성 서비스

  private final AttributeDefRepository attributeDefRepository;

  private final AttributeDefMapper attributeDefMapper;


  @Transactional
  public ClothesAttributeDefDto create(
      ClothesAttributeDefCreateRequest clothesAttributeDefCreateRequest) {

    //중복 의상 속성 네임 검증
    if (attributeDefRepository.existsByName(clothesAttributeDefCreateRequest.name())) {
      throw new AttributeDefAlreadyExistsException();
    }

    AttributeDef attributeDef = new AttributeDef(
        clothesAttributeDefCreateRequest.name(),
        clothesAttributeDefCreateRequest.selectableValues().toArray(new String[0])
    );

    return attributeDefMapper.toDto(attributeDefRepository.save(attributeDef));

  }

  //TODO: 수정

  //TODO: 조회

  @Transactional
  public ClothesAttributeDefDto delete(UUID attributeDefId) {

    AttributeDef attributeDef = attributeDefRepository.findById(attributeDefId)
        .orElseThrow(() -> new AttributeDefNotFoundException().withId(attributeDefId));

    ClothesAttributeDefDto clothesAttributeDefDto = attributeDefMapper.toDto(attributeDef);

    attributeDefRepository.deleteById(attributeDefId);

    return clothesAttributeDefDto;
  }

  public PageResponse<ClothesAttributeDefDto> findAll(String cursor, String idAfter, int limit,
      String sortBy,
      String sortDirection, String keywordLike) {

    //의상 목록 가져오기
    List<AttributeDef> attributeDefs = attributeDefRepository.findAllByCursor(cursor, idAfter,
        limit + 1,
        sortBy, sortDirection, keywordLike);

    //실제 size계산 (초과 조회된 1개 제외)
    int fetchedSize = attributeDefs.size();
    boolean hasNext = fetchedSize > limit;

    //실제 보여줄 limit 수만큼 clothes 남기기
    List<AttributeDef> result = hasNext ? attributeDefs.subList(0, limit) : attributeDefs;

    //DTO 변환
    List<ClothesAttributeDefDto> attributeDefDtos = result.stream()
        .map(attributeDefMapper::toDto)
        .toList();
    int size = attributeDefDtos.size();

    //TODO: 매번 호출 비효율 -> 캐싱?
    //totalCount
    //sortBy 는 name 디폴트?
    int totalCount = attributeDefRepository.getTotalCounts(sortBy, keywordLike);

    // next 조회
    // default name?
    String nextCursor = hasNext ? result.get(size - 1).getName() : null;
    String nextIdAfter = hasNext ? result.get(size - 1).getId().toString() : null;

    return new PageResponse<>(attributeDefDtos, nextCursor, nextIdAfter, hasNext, totalCount,
        sortBy, sortDirection);
  }
}
