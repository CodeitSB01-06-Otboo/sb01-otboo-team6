package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ClothesCreateRequest(
    @NotNull
    UUID ownerId,

    @NotBlank(message = "이름을 등록해주세요.")
    @Size(max = 40)
    String name,

    @NotNull
    @Size(max = 20)
    String type,

    List<ClothesAttributeDto> attributes

) {

}
