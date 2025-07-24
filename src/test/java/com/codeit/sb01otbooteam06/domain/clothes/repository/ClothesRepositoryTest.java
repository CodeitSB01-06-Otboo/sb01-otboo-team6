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
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

@SpringJUnitConfig
@Import(ClothesRepositoryTest.QuerydslTestConfig.class)
public class ClothesRepositoryTest extends PostgresTestContainer {

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

    @Bean
    public ClothesCustomRepository clothesCustomRepository(JPAQueryFactory factory) {
      return new ClothesCustomRepositoryImpl(factory);
    }
  }

  @Autowired
  EntityProvider entityProvider;

  @Autowired
  ClothesCustomRepository clothesCustomRepository;

  private User user;

  @BeforeEach
  void setUp() {

    em.createQuery("DELETE FROM ClothesAttribute").executeUpdate();
    em.createQuery("DELETE FROM AttributeDef").executeUpdate();
    em.createQuery("DELETE FROM Clothes").executeUpdate();
    em.createQuery("DELETE FROM Profile").executeUpdate();
    em.createQuery("DELETE FROM User").executeUpdate();

    user = entityProvider.createTestUser();
    Profile profile = entityProvider.createTestProfile(user);
    user.setProfile(profile);
    Clothes clothes = entityProvider.createTestClothes(user);
    AttributeDef attrDefs = entityProvider.createTestAttributeDef("계절",
        List.of("봄", "여름", "가을", "겨울"));
    ClothesAttribute clothesAttr = entityProvider.createTestClothesAttribute(clothes, attrDefs,
        "여름");

    // 날짜 설정
    ReflectionTestUtils.setField(user, "createdAt", Instant.now());
    ReflectionTestUtils.setField(profile, "createdAt", Instant.now());
    ReflectionTestUtils.setField(clothes, "createdAt", Instant.now());
    ReflectionTestUtils.setField(attrDefs, "createdAt", Instant.now());
    ReflectionTestUtils.setField(clothesAttr, "createdAt", Instant.now());

    em.persist(user);
    em.persist(profile);
    em.persist(attrDefs);
    em.persist(clothes);
    em.persist(clothesAttr);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("findAllByCursor - 의상 조회 성공")
  void testFindAllByCursor() {
    List<Clothes> result = clothesCustomRepository.findAllByCursor(
        null, null, 10, "TOP", user.getId());

    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getOwner().getId()).isEqualTo(user.getId());
  }

  @Test
  @DisplayName("getTotalCounts - 의상 개수 반환")
  void testGetTotalCounts() {
    int count = clothesCustomRepository.getTotalCounts("TOP", user.getId());

    assertThat(count).isGreaterThan(0);
  }

  @Test
  @DisplayName("findAllByOwnerWithValue - 속성 기반 의상 추천 성공")
  void testFindAllByOwnerWithValue() {
    // 테스트용 추천 인덱스 (e.g., "여름", "얇음", "없음", "따뜻하지 않음" 등 가정)
    int[] weightData = {1, 0, 0, 0}; // "여름"으로 매칭되도록

    List<Clothes> result = clothesCustomRepository.findAllByOwnerWithValue(user, weightData);

    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getOwner().getId()).isEqualTo(user.getId());
  }
}
