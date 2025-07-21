package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;

public record ClothesUpdateRequest(
    String name,
    String type,
    List<ClothesAttributeDto> attributes
) {

}
