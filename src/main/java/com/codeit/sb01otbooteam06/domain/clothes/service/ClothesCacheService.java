package com.codeit.sb01otbooteam06.domain.clothes.service;

import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothesCacheService {

  private final ClothesRepository clothesRepository;
  private final CacheManager cacheManager;

  //조회 페이지네이션 - 유저 옷장의 옷 개수 캐시
  @Cacheable(value = "PageUserClothesCount", key = "#userId")
  public int getPageUserClothesCountWithCache(String typeEqual, UUID userId) {
    return clothesRepository.getTotalCounts(typeEqual, userId);
  }

  //조회 페이지네이션 - 유저 옷장의 옷 개수 캐시 삭제
  @CacheEvict(value = "PageUserClothesCount", key = "#userId")
  public void invalidatePageUserCurrentClothesCountCache(UUID userId) {
  }


  // 의상 추천시 현재 유저의 옷 개수 캐시에 저장
  public void saveUserCurrentClothesCountCache(UUID userId, int currentClothesCount) {
    cacheManager.getCache("userClothesCount").put(userId, currentClothesCount);
  }

  // 호출시 현재 유저의 의상 수 캐시 제거됨 (예: 옷 추가/삭제 시)
  @CacheEvict(value = "userClothesCount", key = "#userId")
  public void invalidateUserCurrentClothesCountCache(UUID userId) {
  }


}
