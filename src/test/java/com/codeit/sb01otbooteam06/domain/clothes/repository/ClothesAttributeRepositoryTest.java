package com.codeit.sb01otbooteam06.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import com.codeit.sb01otbooteam06.util.PostgresTestContainer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@Transactional
@Import(ClothesAttributeRepositoryTest.QuerydslTestConfig.class)
class ClothesAttributeRepositoryTest extends PostgresTestContainer {

  @PersistenceContext
  EntityManager em;

  @TestConfiguration
  static class QuerydslTestConfig {

    @PersistenceContext
    EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(em);
    }

    @Bean
    public EntityProvider entityProvider() {
      return new EntityProvider();
    }
  }

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

    ReflectionTestUtils.setField(user, "createdAt", Instant.now());
    ReflectionTestUtils.setField(profile, "createdAt", Instant.now());
    ReflectionTestUtils.setField(clothes, "createdAt", Instant.now());
    ReflectionTestUtils.setField(attributeDef, "createdAt", Instant.now());
    ReflectionTestUtils.setField(clothesAttribute, "createdAt", Instant.now());
    // 저장 및 플러시 (엔티티 매니저 또는 리포지토리 이용)
    em.persist(user);
    em.persist(profile);
    em.persist(clothes);
    em.persist(attributeDef);
    em.persist(clothesAttribute);
    em.flush();
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
