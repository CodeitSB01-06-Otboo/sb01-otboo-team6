package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import java.util.List;

public record ClothesAttributeDefCreateRequest(
    String name,
    List<String> selectableValues
) {

}
