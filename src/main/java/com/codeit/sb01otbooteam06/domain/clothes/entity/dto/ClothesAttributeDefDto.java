package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ClothesAttributeDefDto(
    UUID id,
    @NotNull
    String name,
    List<String> selectableValues
) {

}
