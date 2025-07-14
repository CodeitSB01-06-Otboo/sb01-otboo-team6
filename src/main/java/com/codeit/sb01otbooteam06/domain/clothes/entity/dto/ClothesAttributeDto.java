package com.codeit.sb01otbooteam06.domain.clothes.entity.dto;


//의상 등록시 의상에 대한 속성 밸류 정보 (중간 테이블 생성용 )
public record ClothesAttributeDto(
    String definitionId,
    String value
) {

}
