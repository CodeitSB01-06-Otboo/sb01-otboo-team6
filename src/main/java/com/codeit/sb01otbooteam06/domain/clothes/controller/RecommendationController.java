package com.codeit.sb01otbooteam06.domain.clothes.controller;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.RecommendationDto;
import com.codeit.sb01otbooteam06.domain.clothes.service.RecommendService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

  private final RecommendService recommendService;

  @GetMapping
  public ResponseEntity<RecommendationDto> getRecommendations(
      @RequestParam("weatherId") UUID weatherId
  ) {
    RecommendationDto recommendationDto = recommendService.recommend(weatherId);
    return ResponseEntity.ok(recommendationDto);
  }

}
