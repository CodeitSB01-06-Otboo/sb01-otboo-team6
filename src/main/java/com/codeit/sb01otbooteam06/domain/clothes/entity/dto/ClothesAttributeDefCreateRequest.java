package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ClothesAttributeDefCreateRequest(
    @NotNull
    String name,
    List<String> selectableValues
) {

}
