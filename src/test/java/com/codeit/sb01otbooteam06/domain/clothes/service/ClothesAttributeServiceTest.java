package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDto;
import com.codeit.sb01otbooteam06.domain.clothes.exception.AttributeDefNotFoundException;
import com.codeit.sb01otbooteam06.domain.clothes.repository.AttributeDefRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ClothesAttributeServiceTest {

  @InjectMocks
  private ClothesAttributeService clothesAttributeService;

  @Mock
  private ClothesAttributeRepository clothesAttributeRepository;

  @Mock
  private AttributeDefRepository attributeDefRepository;

  private Clothes clothes;
  private UUID attrDefId;
  private AttributeDef attrDef;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    User user = EntityProvider.createTestUser();
    Profile profile = EntityProvider.createTestProfile(user);
    clothes = EntityProvider.createCustomTestClothes(user, "상의", "TOP", "image.url");
    attrDef = EntityProvider.createTestAttributeDef("속성", List.of("속성", "속성2"));
    attrDefId = UUID.randomUUID();
  }

  @Test
  void create_validAttributes_shouldReturnSavedAttributes() {
    // given
    ClothesAttributeDto dto = new ClothesAttributeDto(attrDefId.toString(), "면");
    List<ClothesAttributeDto> dtoList = List.of(dto);

    when(attributeDefRepository.findById(attrDefId)).thenReturn(Optional.of(attrDef));
    when(clothesAttributeRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    List<ClothesAttribute> result = clothesAttributeService.create(clothes, dtoList);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getValue()).isEqualTo("면");
    assertThat(result.get(0).getAttributeDef()).isEqualTo(attrDef);
    verify(clothesAttributeRepository).saveAll(anyList());
  }

  @Test
  void create_invalidDefinitionId_shouldThrowException() {
    // given
    UUID invalidId = UUID.randomUUID();
    ClothesAttributeDto dto = new ClothesAttributeDto(invalidId.toString(), "울");
    when(attributeDefRepository.findById(invalidId)).thenReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> clothesAttributeService.create(clothes, List.of(dto)))
        .isInstanceOf(AttributeDefNotFoundException.class);
  }

  @Test
  void update_shouldDeleteAndRecreateAttributes() {
    // given
    ClothesAttributeDto dto = new ClothesAttributeDto(attrDefId.toString(), "합성섬유");
    List<ClothesAttributeDto> dtoList = List.of(dto);

    when(attributeDefRepository.findById(attrDefId)).thenReturn(Optional.of(attrDef));
    when(clothesAttributeRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    List<ClothesAttribute> result = clothesAttributeService.update(clothes, dtoList);

    // then
    verify(clothesAttributeRepository).deleteByClothes(clothes);
    verify(clothesAttributeRepository).saveAll(anyList());
    assertThat(result).hasSize(1);
  }
}
