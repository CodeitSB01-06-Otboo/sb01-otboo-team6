package com.codeit.sb01otbooteam06.domain.clothes.service;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesCreateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesDto;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.ClothesUpdateRequest;
import com.codeit.sb01otbooteam06.domain.clothes.entity.dto.PageResponse;
import com.codeit.sb01otbooteam06.domain.clothes.exception.ClothesNotFoundException;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesAttributeWithDefDtoMapper;
import com.codeit.sb01otbooteam06.domain.clothes.mapper.ClothesMapper;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesAttributeRepository;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.clothes.utils.ClothesUtils;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.exception.UserNotFoundException;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesService {

  private final ClothesRepository clothesRepository;
  private final UserRepository userRepository;

  private final ClothesAttributeService clothesAttributeService;

  private final ClothesMapper clothesMapper;
  private final ClothesAttributeWithDefDtoMapper clothesAttributeWithDefDtoMapper;

  private final ClothesUtils clothesUtils;

  //S3 이미지 저장 디렉토리 네임
  private final String directory = "Clothes";
  private final ClothesAttributeRepository clothesAttributeRepository;


  /**
   * 의상을 등록합니다.
   *
   * @param clothesCreateRequest
   * @param clothesImage
   * @return ClothesDto
   */
  @Transactional
  public ClothesDto create(ClothesCreateRequest clothesCreateRequest, MultipartFile clothesImage) {

    //유저 찾기
    User owner = userRepository.findById(clothesCreateRequest.ownerId())
        .orElseThrow(() -> new UserNotFoundException(clothesCreateRequest.ownerId()));

    //TODO: S3 업로드 로직 필요
    String imageUrl = "";
    //String imageUrl = s3Service.upload(clothesImage, directory);

    Clothes clothes = new Clothes(
        owner,
        clothesCreateRequest.name(),
        clothesCreateRequest.type(),
        imageUrl
    );

    //DB에 의상 엔티티 저장
    clothesRepository.save(clothes);

    List<ClothesAttribute> clothesAttributes = clothesAttributeService.create(clothes,
        clothesCreateRequest.attributes());

    return clothesUtils.makeClothesDto(clothes, clothesAttributes);
  }


  /**
   * 커서 기반 페이지네이션으로 의상 목록을 조회합니다.
   *
   * @param cursor
   * @param idAfter
   * @param limit
   * @param typeEqual
   * @param ownerId
   * @return PageResponse<ClothesDto>
   */
  @Transactional(readOnly = true)
  public PageResponse<ClothesDto> findAll(String cursor, String idAfter,
      int limit, String typeEqual, UUID ownerId) {

    //의상 목록 가져오기
    List<Clothes> clothesList = clothesRepository.findAllByCursor(cursor, idAfter, limit + 1,
        typeEqual, ownerId);

    //실제 size계산 (초과 조회된 1개 제외)
    int fetchedSize = clothesList.size();
    boolean hasNext = fetchedSize > limit;

    //실제 보여줄 limit 수만큼 clothes 남기기
    List<Clothes> resultClothes = hasNext ? clothesList.subList(0, limit) : clothesList;

    ///  dto 변환 로직
    //결과를 담을 clothesDto리스트 
    List<ClothesDto> clothesDtos = new ArrayList<>();

    //todo: n+1문제

    // 의상 결과 리스트에 대하여 dto 변환 수행
    for (Clothes clothes : resultClothes) {
      //의상에 대한 속성
      List<ClothesAttribute> clothesAttributes = clothesAttributeRepository.findByClothes(clothes);
      //결과리스트에 clothesdto추가
      clothesDtos.add(clothesUtils.makeClothesDto(clothes, clothesAttributes)
      );
    }
    ///

    int size = resultClothes.size();

    //TODO: 매번 호출 비효율 -> 캐싱?
    //totalCount
    int totalCount = clothesRepository.getTotalCounts(typeEqual, ownerId);

    // next 조회
    String nextCursor = hasNext ? resultClothes.get(size - 1).getCreatedAt().toString() : null;
    String nextIdAfter = hasNext ? resultClothes.get(size - 1).getId().toString() : null;

    return new PageResponse<>(clothesDtos, nextCursor, nextIdAfter, hasNext, totalCount,
        "createdAt", "DESCENDING");

  }

  /**
   * 의상을 수정합니다.
   *
   * @param clothesID
   * @param clothesUpdateRequest
   * @param clothesImage
   * @return ClothesDto
   */
  @Transactional
  public ClothesDto update(UUID clothesID, ClothesUpdateRequest clothesUpdateRequest,
      MultipartFile clothesImage) {

    //의상 존재 확인
    Clothes clothes = clothesRepository.findById(clothesID)
        .orElseThrow(() -> new ClothesNotFoundException().withId(clothesID));

    // name, type의 수정이 없는 경우 예외처리
    // todo: 엔티티단으로 책임 넘기기?
    String newName = clothesUpdateRequest.name();
    String newType = clothesUpdateRequest.type();
    if (clothesUpdateRequest.name() == null) {
      newName = clothes.getName();
    }
    if (clothesUpdateRequest.type() == null) {
      newType = clothes.getType();
    }

    // todo: 이미지가 새로 들어온 경우에 S3에 업로드
    String imageUrl = clothes.getImageUrl();
//    if (clothesImage != null) {
//      imageUrl = s3Service.upload(clothesImage, directory);
//    }

    //의상 정보 업데이트
    clothes.update(
        newName,
        newType,
        imageUrl
    );

    // 의상에 대한 의상속성중간테이블 업데이트
    List<ClothesAttribute> clothesAttributes =
        clothesAttributeService.update(clothes, clothesUpdateRequest.attributes());

    return clothesUtils.makeClothesDto(clothes, clothesAttributes);


  }

  //TODO: (심화) 구매 링크로 의상정보 불러오기?

  /**
   * 의상을 삭제합니다.
   *
   * @param clothesId
   */
  @Transactional
  public void delete(UUID clothesId) {
    clothesRepository.findById(clothesId)
        .orElseThrow(() -> new ClothesNotFoundException().withId(clothesId));
    clothesRepository.deleteById(clothesId);

  }

}
