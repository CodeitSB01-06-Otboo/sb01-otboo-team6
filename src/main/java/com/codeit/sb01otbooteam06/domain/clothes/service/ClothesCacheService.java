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

  //유저 옷장의 옷 개수 캐시 //todo 페이지네이션에서 이용하기!
  @Cacheable(value = "userClothesCount", key = "#userId")
  public int getUserClothesCountWithCache(UUID userId) {
    return clothesRepository.getTotalCounts("", userId);
  }

  public int getUserClothesCount(UUID userId) {
    return clothesRepository.getTotalCounts("", userId);
  }

  // 호출시 유저의 의상 수 캐시 제거됨 (예: 옷 추가/삭제 시)
  @CacheEvict(value = "userClothesCount", key = "#userId")
  public void invalidateUserClothesCache(UUID userId) {
  }

  // 호출시 캐시에 저장
  public void saveCache(UUID userId, int currentClothesCount) {
    cacheManager.getCache("userClothesCount").put(userId, currentClothesCount);
  }


}
