package com.codeit.sb01otbooteam06.domain.clothes.controller;

import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.service.ClothesService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clothes")
public class ClothesController {

  private final ClothesService clothesService;

  //TODO: VAlid 설정
  @PostMapping
  public ResponseEntity<ClothesDto> create(
      @RequestPart(value = "request")
      ClothesCreateRequest clothesCreateRequest,
      @RequestPart(value = "image", required = false) MultipartFile file) {
    ClothesDto clothesDto = clothesService.create(clothesCreateRequest, file);
    return ResponseEntity.status(HttpStatus.CREATED).body(clothesDto);
  }

  @GetMapping
  public ResponseEntity<PageResponse<ClothesDto>> findAll(
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "idAfter", required = false) String idAfter,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "typeEqual", defaultValue = "TOP") String typeEqual,
      @RequestParam(value = "ownerId") UUID ownerId
  ) {
    PageResponse<ClothesDto> result = clothesService.findAll(cursor, idAfter, limit, typeEqual,
        ownerId);
    return ResponseEntity.ok(result);

  }

  @PatchMapping("/{clothesId}")
  public ResponseEntity<ClothesDto> update(@PathVariable("clothesId") UUID clothesId,
      @RequestPart("request") ClothesUpdateRequest clothesUpdateRequest,
      @RequestPart(value = "image", required = false) MultipartFile file) {
    ClothesDto clothesDto = clothesService.update(clothesId, clothesUpdateRequest, file);
    return ResponseEntity.ok(clothesDto);
  }

  @DeleteMapping("/{clothesId}")
  public ResponseEntity<Void> delete(@PathVariable("clothesId") UUID clothesId) {
    clothesService.delete(clothesId);
    return ResponseEntity.noContent().build();

  }

}
