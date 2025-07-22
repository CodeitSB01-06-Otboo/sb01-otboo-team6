package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.CustomClothesUtils;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.codeit.sb01otbooteam06.global.s3.S3Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ClothesServiceTest {

  @Mock
  private S3Service s3Service;
  @Mock
  private ClothesCacheService clothesCacheService;
  @Mock
  private ClothesRepository clothesRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ClothesAttributeRepository clothesAttributeRepository;
  @Mock
  private ClothesAttributeService clothesAttributeService;
  @Mock
  private CustomClothesUtils customClothesUtils;
  @Mock
  JsoupService jsoupService;

  @InjectMocks
  private ClothesService clothesService;

  @Test
  void create_shouldSaveClothesAndReturnDto() throws Exception {
    // given
    UUID ownerId = UUID.randomUUID();
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(ownerId);  //
    when(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser));

    ClothesCreateRequest req = new ClothesCreateRequest(ownerId, "Name", "Type", List.of());
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.isEmpty()).thenReturn(false);
    when(s3Service.upload(mockFile, "Clothes")).thenReturn("http://image.url");

    Clothes savedClothes = new Clothes(mockUser, "Name", "Type", "http://image.url");
    when(clothesRepository.save(any())).thenReturn(savedClothes);

    List<ClothesAttribute> attributes = new ArrayList<>();
    when(clothesAttributeService.create(any(), any())).thenReturn(attributes);

    ClothesDto expectedDto = new ClothesDto(null, null, "Name", "http://image.url", null, null);
    when(customClothesUtils.makeClothesDto(any(Clothes.class), anyList()))
        .thenReturn(expectedDto);

    // when
    ClothesDto result = clothesService.create(req, mockFile);

    // then
    assertEquals(expectedDto, result);
    verify(clothesCacheService).invalidateUserCurrentClothesCountCache(ownerId);
  }

  @Test
  void findAll_shouldReturnPagedClothesDto() {
    // given
    UUID ownerId = UUID.randomUUID();
    Clothes mockClothes = mock(Clothes.class);
    when(clothesRepository.findAllByCursor(
        any(),     // cursor
        any(),     // idAfter
        anyInt(),  // limit
        any(),     // typeEqual
        eq(ownerId)
    )).thenReturn(List.of(mockClothes));

    ClothesAttribute attr = mock(ClothesAttribute.class);
    when(clothesAttributeRepository.findByClothes(mockClothes)).thenReturn(List.of(attr));
    ClothesDto dto = mock(ClothesDto.class);
    when(customClothesUtils.makeClothesDto(mockClothes, List.of(attr))).thenReturn(dto);

    when(clothesCacheService.getPageUserClothesCountWithCache(
        any(),       // typeEqual (null 포함 가능)
        eq(ownerId)  // 정확한 UUID 일치
    )).thenReturn(1);

    // when
    PageResponse<ClothesDto> response = clothesService.findAll(null, null, 1, null, ownerId);

    // then
    assertNotNull(response);
    assertEquals(dto, response.getData().get(0));
  }

  @Test
  void update_shouldUpdateAndReturnDto() {
    // given
    UUID clothesId = UUID.randomUUID();
    ClothesUpdateRequest req = new ClothesUpdateRequest("UpdatedName", "UpdatedType", List.of());
    MultipartFile mockFile = mock(MultipartFile.class);

    Clothes clothes = mock(Clothes.class);
    when(clothesRepository.findById(clothesId)).thenReturn(Optional.of(clothes));

    when(s3Service.upload(mockFile, "Clothes")).thenReturn("http://new.image.url");

    List<ClothesAttribute> updatedAttrs = new ArrayList<>();
    when(clothesAttributeService.update(clothes, req.attributes())).thenReturn(updatedAttrs);

    ClothesDto dto = mock(ClothesDto.class);
    when(customClothesUtils.makeClothesDto(clothes, updatedAttrs)).thenReturn(dto);

    // when
    ClothesDto result = clothesService.update(clothesId, req, mockFile);

    // then
    assertEquals(dto, result);
    verify(clothes).update("UpdatedName", "UpdatedType", "http://new.image.url");
  }


  @Test
  void delete_shouldDeleteClothesAndInvalidateCache() {
    // given
    UUID clothesId = UUID.randomUUID();
    User owner = mock(User.class);
    UUID ownerId = UUID.randomUUID();
    when(owner.getId()).thenReturn(ownerId);

    Clothes clothes = mock(Clothes.class);
    when(clothes.getOwner()).thenReturn(owner);

    when(clothesRepository.findById(clothesId)).thenReturn(Optional.of(clothes));

    // when
    clothesService.delete(clothesId);

    // then
    verify(clothesRepository).deleteById(clothesId);
    verify(clothesCacheService).invalidateUserCurrentClothesCountCache(ownerId);
  }


  @Test
  void extractByUrl_scriptTagNotNull_shouldParseAndReturnDto() throws Exception {
    String url = "http://someurl.com";

    Document mockDocument = mock(Document.class);
    Element mockScriptTag = mock(Element.class);

    when(jsoupService.getDocument(url)).thenReturn(mockDocument);
    when(mockDocument.selectFirst("script#pdp-data")).thenReturn(mockScriptTag);
    when(mockScriptTag.html()).thenReturn(
        "window.__MSS__.product.state = {\"goodsNm\":\"TestName\", \"thumbnailImageUrl\":\"/image.jpg\"};");

    ClothesDto result = clothesService.extractByUrl(url);

    assertEquals("TestName", result.name());
    assertEquals("https://image.msscdn.net/image.jpg", result.imageUrl());
  }

  @Test
  void getUserClothesCount_shouldReturnCount() {
    // given
    UUID userId = UUID.randomUUID();
    when(clothesRepository.getTotalCounts("", userId)).thenReturn(5);

    // when
    int count = clothesService.getUserClothesCount(userId);

    // then
    assertEquals(5, count);
    verify(clothesRepository).getTotalCounts("", userId);
  }

  @Test
  void findAllById_shouldReturnClothesList() {
    // given
    List<UUID> clothesIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    List<Clothes> clothesList = List.of(mock(Clothes.class), mock(Clothes.class));
    when(clothesRepository.findAllById(clothesIds)).thenReturn(clothesList);

    // when
    List<Clothes> result = clothesService.findAllById(clothesIds);

    // then
    assertEquals(clothesList, result);
    verify(clothesRepository).findAllById(clothesIds);
  }

}

