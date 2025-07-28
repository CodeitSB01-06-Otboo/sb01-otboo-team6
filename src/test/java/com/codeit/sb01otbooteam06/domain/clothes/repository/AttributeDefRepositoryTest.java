package com.codeit.sb01otbooteam06.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.util.EntityProvider;
import com.codeit.sb01otbooteam06.util.PostgresTestContainer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
@Import(AttributeDefRepositoryTest.QuerydslTestConfig.class)
class AttributeDefRepositoryTest extends PostgresTestContainer {

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
  AttributeDefRepository repository;

  @Autowired
  TestEntityManager em;

  @Autowired
  EntityProvider entityProvider;

  @BeforeEach
  void setUp() throws InterruptedException {
    // DB 초기화 (PostgreSQL 기준)
    em.getEntityManager().createQuery("DELETE FROM AttributeDef").executeUpdate();

    // 테스트 데이터 저장
    saveAttribute("Tops", new String[]{"T-shirt", "Long-sleeve"});
    saveAttribute("Bottoms", new String[]{"Jeans", "Slacks"});
    saveAttribute("Accessories", new String[]{"Hat", "Bag"});
    saveAttribute("Colors", new String[]{"Red", "Blue"});
    saveAttribute("Sizes", new String[]{"S", "M", "L"});
    saveAttribute("Materials", new String[]{"Cotton", "Polyester"});
  }

  void saveAttribute(String name, String[] values) throws InterruptedException {
    AttributeDef attr = entityProvider.createTestAttributeDef(name, Arrays.asList(values));
    ReflectionTestUtils.setField(attr, "createdAt", Instant.now());
    Thread.sleep(100);
    em.persist(attr);
  }

  @Test
  @DisplayName("키워드에 해당하는 속성들을 반환한다")
  void findAllByKeyword() {
    List<AttributeDef> results = repository.findAllByCursor(
        null, null, 10, "name", "ASCENDING", "Bot");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("Bottoms");
  }

  @DisplayName("전체 속성 개수를 반환한다")
  @Test
  void getTotalCounts() {
    // given
    // @BeforeEach에서 이미 6개 저장됐다고 가정

    // when
    int count = repository.getTotalCounts("name", "");

    // then
    assertThat(count).isEqualTo(6);
  }

  @DisplayName("키워드로 필터링된 속성 개수를 반환한다")
  @Test
  void getTotalCounts_withKeyword() {
    // given
    String keyword = "Bot";

    // when
    int count = repository.getTotalCounts("name", keyword);

    // then
    assertThat(count).isEqualTo(1);
  }


  @Test
  @DisplayName("이름 기준 내림차순 정렬 후 커서 기반으로 필터링한다")
  void findAllByCursorDesc() {
    // Given: 전체 데이터를 내림차순으로 조회
    List<AttributeDef> allDesc = repository.findAllByCursor(
        null, null, 10, "name", "DESCENDING", null);

    // 정렬 검증
    assertThat(allDesc)
        .isSortedAccordingTo((a1, a2) -> {
          int nameCompare = a2.getName().compareTo(a1.getName());
          if (nameCompare != 0) {
            return nameCompare;
          }
          return a2.getCreatedAt().compareTo(a1.getCreatedAt());
        });

    // When: 첫 번째 아이템을 커서로 사용하여 다음 페이지 조회
    AttributeDef firstItem = allDesc.get(0);
    String cursor = firstItem.getName();

    List<AttributeDef> filtered = repository.findAllByCursor(
        cursor, null, 10, "name", "DESCENDING", null);

    // Then: 필터링된 결과 검증
    assertThat(filtered)
        .allMatch(attr -> attr.getName().compareTo(cursor) < 0)  // 이름이 커서보다 작아야 함
        .doesNotContain(firstItem);  // 첫 번째 아이템은 포함되지 않아야 함

    // 추가 검증: 필터링된 결과도 정렬되어 있어야 함
    assertThat(filtered)
        .isSortedAccordingTo((a1, a2) -> {
          int nameCompare = a2.getName().compareTo(a1.getName());
          if (nameCompare != 0) {
            return nameCompare;
          }
          return a2.getCreatedAt().compareTo(a1.getCreatedAt());
        });
  }
}