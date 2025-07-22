package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class ClothesServiceTest {

  @Mock
  private ClothesRepository clothesRepository;

  @Mock
  private ClothesCacheService clothesCacheService;

  @InjectMocks
  private ClothesService clothesService;

  // User 생성
  User user = EntityProvider.createTestUser();
  // Profile 생성
  Profile profile = EntityProvider.createTestProfile(user);

  Clothes clothes = EntityProvider.createCustomTestClothes(user, "상의", "TOP", "image.url");

  @BeforeEach
  void setUp() {

  }


  @Test
  @DisplayName("의상 삭제 테스트")
  public void testDeleteClothes() {

    // given
    UUID clothesId = clothes.getId();

    given(clothesRepository.findById(clothesId)).willReturn(Optional.of(clothes));

    // when
    clothesService.delete(clothesId);

    // then
    verify(clothesRepository).deleteById(clothesId);
    verify(clothesCacheService).invalidateUserCurrentClothesCountCache(clothes.getOwner().getId());
  }

}
