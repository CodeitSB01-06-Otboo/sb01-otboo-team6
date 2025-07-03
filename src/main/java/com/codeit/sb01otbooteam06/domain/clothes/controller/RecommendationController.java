package com.codeit.sb01otbooteam06.domain.clothes.controller;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.RecommendationDto;
import com.codeit.sb01otbooteam06.domain.clothes.service.RecommendationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendationService recommendationService;

  /**
   * Retrieves clothing recommendations based on the specified weather ID.
   *
   * @param weatherId the unique identifier of the weather condition for which recommendations are requested
   * @return a ResponseEntity containing the recommended clothing information
   */
  @GetMapping
  public ResponseEntity<RecommendationDto> getRecommendations(
      @PathVariable("weatherId") UUID weatherId
  ) {
    RecommendationDto recommendationDto = recommendationService.create(weatherId);
    return ResponseEntity.ok(recommendationDto);
  }

}
