package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;

public record ClothesAttributeDefDto(
    String id,
    String name,
    List<String> selectableValues
) {

}
