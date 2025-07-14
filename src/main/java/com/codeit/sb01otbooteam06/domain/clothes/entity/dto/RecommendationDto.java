package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;
import java.util.UUID;

public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<OotdDto> clothes
) {

}
