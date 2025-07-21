package com.codeit.sb01otbooteam06.domain.clothes.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.RecommendationDto;
import com.codeit.sb01otbooteam06.domain.clothes.service.RecommendService;
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
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(controllers = RecommendationController.class)
@Import(RecommendationControllerTest.MockConfig.class)
@AutoConfigureMockMvc(addFilters = false)  // Security 필터 비활성화
class RecommendationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private RecommendService recommendService;

  @TestConfiguration
  static class MockConfig {

    @Bean
    public RecommendService recommendService() {
      return Mockito.mock(RecommendService.class);
    }
  }

  @Test
  @DisplayName("GET /api/recommendations - 정상 호출 테스트")
  void getRecommendations_success() throws Exception {
    UUID weatherId = UUID.randomUUID();
    RecommendationDto mockDto = new RecommendationDto(UUID.randomUUID(), UUID.randomUUID(),
        List.of());

    Mockito.when(recommendService.recommend(weatherId)).thenReturn(mockDto);

    mockMvc.perform(get("/api/recommendations")
            .param("weatherId", weatherId.toString()))
        .andExpect(status().isOk());
  }
}