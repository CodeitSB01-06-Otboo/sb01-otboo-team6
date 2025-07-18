package com.codeit.sb01otbooteam06.domain.clothes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.service.AttributeDefService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AttributeDefController.class)
@Import(AttributeDefControllerTest.MockConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AttributeDefControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private AttributeDefService attributeDefService;

  @Autowired
  private ObjectMapper objectMapper;

  @TestConfiguration
  static class MockConfig {

    @Bean
    public AttributeDefService attributeDefService() {
      return Mockito.mock(AttributeDefService.class);
    }
  }


  @Test
  @DisplayName("POST /api/clothes/attribute-defs - Create 테스트")
  void createAttributeDef() throws Exception {
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("테스트",
        List.of());
    ClothesAttributeDefDto response = new ClothesAttributeDefDto(UUID.randomUUID(), "테스트",
        List.of());

    given(attributeDefService.create(request)).willReturn(response);

    mockMvc.perform(post("/api/clothes/attribute-defs")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("테스트"));
  }

  @Test
  @DisplayName("GET /api/clothes/attribute-defs - findAll 테스트")
  void findAllAttributeDefs() throws Exception {
    ClothesAttributeDefDto dto = new ClothesAttributeDefDto(UUID.randomUUID(), "테스트", List.of());
    PageResponse<ClothesAttributeDefDto> pageResponse = new PageResponse<>(List.of(dto), null, null,
        false, 20, null, null);

    given(attributeDefService.findAll(any(), any(), anyInt(), any(), any(), any()))
        .willReturn(pageResponse);

    mockMvc.perform(get("/api/clothes/attribute-defs")
            .param("sortBy", "name"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name").value("테스트"));
  }

  @Test
  @DisplayName("PATCH /api/clothes/attribute-defs/{definitionId} - Update 테스트")
  void updateAttributeDef() throws Exception {
    UUID definitionId = UUID.randomUUID();
    ClothesAttributeDefUpdateRequest updateRequest = new ClothesAttributeDefUpdateRequest("수정된 이름",
        List.of());
    ClothesAttributeDefDto response = new ClothesAttributeDefDto(definitionId, "수정된 이름", List.of());

    given(attributeDefService.update(definitionId, updateRequest)).willReturn(response);

    mockMvc.perform(patch("/api/clothes/attribute-defs/{definitionId}", definitionId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(definitionId.toString()))
        .andExpect(jsonPath("$.name").value("수정된 이름"));
  }

  @Test
  @DisplayName("DELETE /api/clothes/attribute-defs/{definitionId} - Delete 테스트")
  void deleteAttributeDef() throws Exception {
    UUID definitionId = UUID.randomUUID();

    mockMvc.perform(delete("/api/clothes/attribute-defs/{definitionId}", definitionId))
        .andExpect(status().isNoContent());
  }
}
