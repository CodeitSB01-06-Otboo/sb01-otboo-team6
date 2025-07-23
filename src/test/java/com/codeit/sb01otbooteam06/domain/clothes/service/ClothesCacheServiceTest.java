package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class ClothesCacheServiceTest {

  @InjectMocks
  private ClothesCacheService clothesCacheService;

  @Mock
  private ClothesRepository clothesRepository;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache cache;

  private UUID userId;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userId = UUID.randomUUID();
  }

  @Test
  void getPageUserClothesCountWithCache_캐시를_사용해_사용자의_의상_총개수를_가져온다() {
    // given
    when(clothesRepository.getTotalCounts("TOP", userId)).thenReturn(5);

    // when
    int count = clothesCacheService.getPageUserClothesCountWithCache("TOP", userId);

    // then
    assertThat(count).isEqualTo(5);
    verify(clothesRepository, times(1)).getTotalCounts("TOP", userId);
  }

  @Test
  void saveUserCurrentClothesCountCache_사용자의_현재_의상_개수_캐시에_저장한다
      () {
    // given
    when(cacheManager.getCache("userClothesCount")).thenReturn(cache);

    // when
    clothesCacheService.saveUserCurrentClothesCountCache(userId, 10);

    // then
    verify(cache).put(userId, 10);
  }

  @Test
  void invalidateUserCurrentClothesCountCache_사용자의_현재_의상_개수_캐시를_무효화한다

      () {
    // given
    when(cacheManager.getCache("userClothesCount")).thenReturn(cache);

    // when
    clothesCacheService.invalidateUserCurrentClothesCountCache(userId);

  }

  @Test
  void invalidatePageUserCurrentClothesCountCache_페이지_사용자의_현재_의상_개수_캐시를_무효화한다() {
    // given / when
    clothesCacheService.invalidatePageUserCurrentClothesCountCache(userId);

  }
}
