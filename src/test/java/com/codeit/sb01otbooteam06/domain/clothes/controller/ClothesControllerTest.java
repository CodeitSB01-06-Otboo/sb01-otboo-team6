package com.codeit.sb01otbooteam06.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.service.ClothesService;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(controllers = ClothesController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ClothesControllerTest.MockConfig.class)
class ClothesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ClothesService clothesService;

  @Autowired
  private ObjectMapper objectMapper;

  @TestConfiguration
  static class MockConfig {

    @Bean
    public ClothesService clothesService() {
      return Mockito.mock(ClothesService.class);
    }
  }

  private ClothesDto clothesDto;
  private ClothesCreateRequest clothesCreateRequest;
  private User user;
  private EntityProvider entityProvider;

  @BeforeEach
  void setUp() {
    UUID ownerId = UUID.randomUUID();
    UUID clothesId = UUID.randomUUID();
    clothesDto = new ClothesDto(
        ownerId,
        clothesId,
        "테스트 옷",
        "http://example.com/image.jpg",
        "TOP",
        List.of()
    );

    user = entityProvider.createTestUser();

    clothesCreateRequest = new ClothesCreateRequest(ownerId, "테스으 옷", "TOP", List.of());

  }

  @Test
  @DisplayName("POST /api/clothes - 옷 생성 성공 (이미지 포함)")
  void createClothes_WithImage_Success() throws Exception {
    // Given
    String requestJson = objectMapper.writeValueAsString(clothesCreateRequest);

    MockMultipartFile requestPart = new MockMultipartFile(
        "request",
        "request.json",
        "application/json",
        requestJson.getBytes(StandardCharsets.UTF_8)
    );

    MockMultipartFile imagePart = new MockMultipartFile(
        "image",
        "test-image.jpg",
        "image/jpeg",
        "fake-image-content".getBytes()
    );

    given(clothesService.create(any(ClothesCreateRequest.class), any(MultipartFile.class)))
        .willReturn(clothesDto);

    // When & Then
    mockMvc.perform(multipart("/api/clothes")
            .file(requestPart)
            .file(imagePart)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andDo(print()) // 디버깅용
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("테스트 옷"))
        .andExpect(jsonPath("$.imageUrl").exists());

    // Verify
    verify(clothesService, times(1)).create(any(ClothesCreateRequest.class),
        any(MultipartFile.class));
  }

  @Test
  @DisplayName("GET /api/clothes - 옷 목록 조회")
  void findAllClothes() throws Exception {
    UUID ownerId = UUID.randomUUID();

    ClothesDto dto = new ClothesDto(UUID.randomUUID(), UUID.randomUUID(), "테스트 옷", "image.url",
        "TOP",
        List.of());

    PageResponse<ClothesDto> pageResponse = new PageResponse<>(List.of(dto), null, null,
        false, 20, null, null);

    given(clothesService.findAll(
        nullable(String.class),  // null 허용
        nullable(String.class),
        any(Integer.class),
        anyString(),
        eq(ownerId)))
        .willReturn(pageResponse);

    mockMvc.perform(get("/api/clothes")
            // param("sortBy", "name") 는 컨트롤러 파라미터에 없으면 빼도 됨
            .param("ownerId", ownerId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("테스트 옷"));
  }

  @Test
  @DisplayName("PATCH /api/clothes/{clothesId} - 옷 수정")
  void updateClothes() throws Exception {
    UUID clothesId = UUID.randomUUID();

    MockMultipartFile requestPart = new MockMultipartFile(
        "request", "", "application/json",
        "{\"name\":\"테스트 옷\"}".getBytes()
    );

    MockMultipartFile imagePart = new MockMultipartFile(
        "image", "updated.jpg", "image/jpeg", "updated-image".getBytes()
    );

    given(clothesService.update(eq(clothesId), any(ClothesUpdateRequest.class), any())).willReturn(
        clothesDto);

    MockMultipartHttpServletRequestBuilder builder =
        (MockMultipartHttpServletRequestBuilder) multipart("/api/clothes/" + clothesId)
            .file(requestPart)
            .file(imagePart)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            });

    mockMvc.perform(builder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("테스트 옷"));
  }

  @Test
  @DisplayName("DELETE /api/clothes/{clothesId} - 옷 삭제")
  void deleteClothes() throws Exception {
    UUID clothesId = UUID.randomUUID();

    mockMvc.perform(delete("/api/clothes/" + clothesId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("GET /api/clothes/extractions - URL로 옷 추출")
  void extractByUrl() throws Exception {
    String url = "http://example.com/item/123";

    given(clothesService.extractByUrl(url)).willReturn(clothesDto);

    mockMvc.perform(get("/api/clothes/extractions")
            .param("url", url))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("테스트 옷"));
  }
}
