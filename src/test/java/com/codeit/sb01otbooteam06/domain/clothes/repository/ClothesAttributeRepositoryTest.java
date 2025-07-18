package com.codeit.sb01otbooteam06.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.sb01otbooteam06.config.JpaAuditingConfiguration;
import com.codeit.sb01otbooteam06.config.QueryDslConfig;
import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Import({QueryDslConfig.class, JpaAuditingConfiguration.class})
class ClothesAttributeRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;//test용 엔티티매니저- 엔티티를 저장 및 조회할때마다 영속성 컨텍스트에 엔티티를 보관하고 관리한다.

  @Autowired
  private ClothesAttributeRepository clothesAttributeRepository;
  ;

  // 클래스 필드 선언만
  static String defName = "계절";
  static List<String> defValues = List.of("봄", "여름", "가을", "계절");

  User user;
  Profile profile;
  AttributeDef attributeDef;
  Clothes clothes;
  ClothesAttribute clothesAttribute;

  @BeforeEach
  void setUp() {
    user = EntityProvider.createTestUser();
    profile = EntityProvider.createTestProfile(user);
    clothes = EntityProvider.createTestClothes(user);
    attributeDef = EntityProvider.createTestAttributeDef(defName, defValues);
    clothesAttribute = EntityProvider.createTestClothesAttribute(clothes, attributeDef, "봄");

    entityManager.persist(user);
    entityManager.persist(profile);
    entityManager.persist(attributeDef);
    entityManager.persist(clothes);
    entityManager.persist(clothesAttribute);
    entityManager.clear();
  }

  @Test
  void findAttributesByClothesIds_데이터_조회_성공() {

    // when
    List<ClothesAttribute> result = clothesAttributeRepository.findAttributesByClothesIds(
        List.of(clothes.getId()));

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getValue()).isEqualTo("봄");
    assertThat(result.get(0).getClothes().getId()).isEqualTo(clothes.getId());
    assertThat(result.get(0).getAttributeDef().getName()).isEqualTo("계절");
  }

  @Test
  void findAttributesByClothesIds_빈_리스트_입력시_빈_리스트_반환() {
    // when
    List<ClothesAttribute> result = clothesAttributeRepository.findAttributesByClothesIds(
        List.of());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void findAttributesByClothesIds_null_입력시_빈_리스트_반환() {
    // when
    List<ClothesAttribute> result = clothesAttributeRepository.findAttributesByClothesIds(
        null);

    // then
    assertThat(result).isEmpty();
  }
}
