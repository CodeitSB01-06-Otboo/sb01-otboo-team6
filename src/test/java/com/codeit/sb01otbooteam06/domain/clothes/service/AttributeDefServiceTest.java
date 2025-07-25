package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.exception.AttributeDefNotFoundException;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.AttributeDefMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.AttributeDefRepository;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class AttributeDefServiceTest {

  @InjectMocks
  private AttributeDefService attributeDefService;

  @Mock
  private AttributeDefRepository attributeDefRepository;

  @Mock
  private AttributeDefMapper attributeDefMapper;


//  @Test
//  void createAttributeDef_정상적으로_Dto를_반환한다() {
//    // given
//    var createRequest = new ClothesAttributeDefCreateRequest("색상", List.of("빨강", "파랑", "초록"));
//
//    AttributeDef savedEntity = EntityProvider.createTestAttributeDef("색상",
//        List.of("빨강", "파랑", "초록"));
//    ClothesAttributeDefDto dto = new ClothesAttributeDefDto(savedEntity.getId(), "색상",
//        savedEntity.getSelectableValues());
//
//    // 목 동작 정의
//    when(attributeDefRepository.save(any(AttributeDef.class))).thenReturn(savedEntity);
//    when(attributeDefMapper.toDto(savedEntity)).thenReturn(dto);
//
//    // when
//    ClothesAttributeDefDto result = attributeDefService.create(createRequest);
//
//    // then
//    assertThat(result.name()).isEqualTo("색상");
//    assertThat(result.selectableValues()).contains("빨강", "파랑", "초록");
//  }

  @Test
  void updateAttributeDef_업데이트된_Dto를_반환한다() {
    // given
    UUID id = UUID.randomUUID();
    var original = EntityProvider.createTestAttributeDef("사이즈", List.of("S", "M", "L"));
    var updatedEntity = EntityProvider.createTestAttributeDef("사이즈",
        List.of("XS", "S", "M", "L", "XL"));
    var updateRequest = new ClothesAttributeDefUpdateRequest("사이즈",
        List.of("XS", "S", "M", "L", "XL"));
    var expectedDto = new ClothesAttributeDefDto(id, "사이즈", updatedEntity.getSelectableValues());

    when(attributeDefRepository.findById(id)).thenReturn(Optional.of(original));
    when(attributeDefRepository.save(original)).thenReturn(updatedEntity);
    when(attributeDefMapper.toDto(updatedEntity)).thenReturn(expectedDto);

    // when
    var updated = attributeDefService.update(id, updateRequest);

    // then
    assertThat(updated.name()).isEqualTo("사이즈");
    assertThat(updated.selectableValues()).contains("XS", "S", "M", "L", "XL");
  }

  @Test
  void deleteAttributeDef_삭제후_다시조회하면_예외가_발생한다() {
    // given
    UUID id = UUID.randomUUID();
    var entity = EntityProvider.createTestAttributeDef("소재", List.of("면", "울"));
    var dto = new ClothesAttributeDefDto(id, "소재", List.of("면", "울"));

    when(attributeDefRepository.findById(id)).thenReturn(Optional.of(entity));
    when(attributeDefMapper.toDto(entity)).thenReturn(dto);

    // when
    attributeDefService.delete(id);

    // then
    when(attributeDefRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(AttributeDefNotFoundException.class, () -> {
      attributeDefService.update(id, new ClothesAttributeDefUpdateRequest("소재", List.of("가죽")));
    });
  }


  @Test
  void findAll_페이징_정렬_조건으로_PageResponse를_반환한다() {
    // given
    String cursor = null;
    String idAfter = null;
    int limit = 2;
    String sortBy = "name";
    String sortDirection = "asc";
    String keywordLike = "사이";

    // 테스트용 데이터 생성
    AttributeDef def1 = EntityProvider.createTestAttributeDef("사이즈", List.of("S", "M", "L"));

    AttributeDef def2 = EntityProvider.createTestAttributeDef("사이즈2", List.of("XS", "XL"));

    AttributeDef def3 = EntityProvider.createTestAttributeDef("사이즈3", List.of("XXL"));

    //id강제세팅
    ReflectionTestUtils.setField(def1, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(def2, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(def3, "id", UUID.randomUUID());

    List<AttributeDef> mockEntities = List.of(def1, def2, def3);

    when(attributeDefRepository.findAllByCursor(cursor, idAfter, limit + 1, sortBy, sortDirection,
        keywordLike))
        .thenReturn(mockEntities);

    when(attributeDefMapper.toDto(def1)).thenReturn(
        new ClothesAttributeDefDto(def1.getId(), def1.getName(), def1.getSelectableValues()));
    when(attributeDefMapper.toDto(def2)).thenReturn(
        new ClothesAttributeDefDto(def2.getId(), def2.getName(), def2.getSelectableValues()));

    when(attributeDefRepository.getTotalCounts(sortBy, keywordLike)).thenReturn(10);

    // when
    PageResponse<ClothesAttributeDefDto> response = attributeDefService.findAll(
        cursor, idAfter, limit, sortBy, sortDirection, keywordLike
    );

    // then
    assertThat(response.isHasNext()).isTrue();
    assertThat(response.getData()).hasSize(limit);
    assertThat(response.getData().get(0).name()).isEqualTo("사이즈");
    assertThat(response.getData().get(1).name()).isEqualTo("사이즈2");
    assertThat(response.getNextCursor()).isEqualTo("사이즈2");
    assertThat(response.getNextIdAfter()).isEqualTo(def2.getId().toString());
    assertThat(response.getTotalCount()).isEqualTo(10);
  }


  @Test
  void getStyleValues_값이_존재하면_리스트를_반환한다() {
    // given
    when(attributeDefRepository.findSelectableValuesByName("스타일"))
        .thenReturn(List.of("캐주얼,포멀"));

    // when
    List<String> styleValues = attributeDefService.getStyleValues();

    // then
    assertThat(styleValues).containsExactly("캐주얼", "포멀");
  }

  @Test
  void getStyleValues_값이_존재하지_않으면_예외를_던진다() {
    // given
    when(attributeDefRepository.findSelectableValuesByName("스타일"))
        .thenReturn(List.of());

    // then
    assertThrows(AttributeDefNotFoundException.class, () -> {
      attributeDefService.getStyleValues();
    });
  }
}