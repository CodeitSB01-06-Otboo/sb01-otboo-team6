package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;
import java.util.UUID;

//의상 조회시 반환하는 attributesDef dto
public record ClothesAttributeWithDefDto(
    UUID definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {

}
