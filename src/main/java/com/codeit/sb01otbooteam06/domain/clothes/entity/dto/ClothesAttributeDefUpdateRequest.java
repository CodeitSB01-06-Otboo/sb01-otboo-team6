package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;

public record ClothesAttributeDefUpdateRequest(
    String name,
    List<String> selectableValues
) {

}
