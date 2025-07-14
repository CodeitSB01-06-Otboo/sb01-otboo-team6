package com.codeit.sb01otbooteam06.domain.clothes.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.repository.ClothesRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class ClothesServiceTest {

  @Mock
  private ClothesRepository clothesRepository;


  @InjectMocks
  private ClothesService clothesService;

  // User 생성
  User user = User.builder()
      .email("test@example.com")
      .password("securePassword123!")
      .name("테스트유저")
      .role(Role.USER)
      .linkedOAuthProviders(List.of())
      .locked(false)
      .build();

  // Profile 생성
  Profile profile = new Profile(
      user,
      "테스트프로필",
      Gender.MALE,
      LocalDate.of(1995, 5, 20),
      37.5665,   // latitude (예: 서울 좌표)
      126.9780,  // longitude
      60,        // x
      127,       // y
      List.of("서울특별시", "중구"),
      5,         // temperatureSensitivity
      null
  );

  Clothes clothes = new Clothes(
      user, "상의", "TOP", null
  );

  @BeforeEach
  void setUp() {

    // User와 Profile 연결
    user.setProfile(profile);
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
  }

}
