package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
    UUID id,
    String name,
    List<String> selectableValues
) {

}
