package com.codeit.sb01otbooteam06.domain.clothes.controller;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesAttributeDefUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.service.AttributeDefService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes/attribute-defs")
public class AttributeDefController {

  private final AttributeDefService attributeDefService;

  @PostMapping
  public ResponseEntity<ClothesAttributeDefDto> create(
      @RequestBody ClothesAttributeDefCreateRequest request) {

    ClothesAttributeDefDto clothesAttributeDefDto = attributeDefService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(clothesAttributeDefDto);

  }

  @GetMapping
  public ResponseEntity<PageResponse<ClothesAttributeDefDto>> findAll(
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "idAfter", required = false) String idAfter,
      @RequestParam(value = "limit", defaultValue = "50") int limit,
      @RequestParam(value = "sortBy") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "ASCENDING") String sortDirection,
      @RequestParam(value = "keywordLike", required = false) String keywordLike
  ) {
    PageResponse<ClothesAttributeDefDto> result = attributeDefService.findAll(cursor, idAfter,
        limit, sortBy, sortDirection, keywordLike);
    return ResponseEntity.ok(result);
  }

  @PatchMapping("/{definitionId}")
  public ResponseEntity<ClothesAttributeDefDto> update(
      @PathVariable UUID definitionId,
      @RequestBody ClothesAttributeDefUpdateRequest updateRequest
  ) {

    ClothesAttributeDefDto clothesAttributeDefDto = attributeDefService.update(definitionId,
        updateRequest);

    return ResponseEntity.ok(clothesAttributeDefDto);
  }

  @DeleteMapping("/{definitionId}")
  public ResponseEntity<Void> delete(
      @PathVariable(value = "definitionId") UUID definitionId) {

    attributeDefService.delete(definitionId);
    return ResponseEntity.noContent().build();

  }

}
