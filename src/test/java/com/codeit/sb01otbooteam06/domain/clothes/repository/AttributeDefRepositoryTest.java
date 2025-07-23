package com.codeit.sb01otbooteam06.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.util.EntityProvider;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})

@Import(AttributeDefRepositoryTest.QuerydslTestConfig.class)
class AttributeDefRepositoryTest {

  @TestConfiguration
  static class QuerydslTestConfig {

    @PersistenceContext
    EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(em);
    }

  }

  @Autowired
  AttributeDefRepository repository;

  @Autowired
  TestEntityManager em;

  private EntityProvider entityProvider;

  private AttributeDef attributeDef1;
  private AttributeDef attributeDef2;
  private AttributeDef attributeDef3;

  @BeforeEach
  void setUp() {
    saveAttribute("상의", new String[]{"반팔", "긴팔"});
    saveAttribute("하의", new String[]{"청바지", "면바지"});
    saveAttribute("모자", new String[]{"비니", "캡"});

    attributeDef1 = entityProvider.createTestAttributeDef("계절",
        List.of("봄", "여름", "가을", "겨울"));
    attributeDef2 = entityProvider.createTestAttributeDef("사이즈",
        List.of("S", "M", "L"));
    attributeDef3 = entityProvider.createTestAttributeDef("색상",
        List.of("검정", "흰색", "빨간색"));

  }

  void saveAttribute(String name, String[] values) {
    AttributeDef attr = entityProvider.createTestAttributeDef(name, Arrays.stream(values).toList());
    ReflectionTestUtils.setField(attr, "createdAt", Instant.now());
    em.persist(attr);
  }

  @Test
  @DisplayName("키워드에 해당하는 속성들을 반환한다")
  void findAllByKeyword() {
    List<AttributeDef> results = repository.findAllByCursor(
        null, null, 10, "name", "ASCENDING", "바지");

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getName()).isEqualTo("하의");
  }

  @Test
  @DisplayName("전체 속성 개수를 반환한다")
  void getTotalCounts() {
    int count = repository.getTotalCounts("name", "");
    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("이름 기준 내림차순 정렬 후 커서 기반으로 필터링한다")
  void findAllByCursorDesc() {
    List<AttributeDef> allDesc = repository.findAllByCursor(
        null, null, 10, "name", "DESCENDING", null);

    assertThat(allDesc).isSortedAccordingTo((a1, a2) -> a2.getName().compareTo(a1.getName()));

    String cursor = allDesc.get(0).getName(); // 가장 큰 이름

    List<AttributeDef> filtered = repository.findAllByCursor(
        cursor, null, 10, "name", "DESCENDING", null);

    assertThat(filtered).hasSize(2); // cursor 제외 나머지 2개
    assertThat(filtered)
        .extracting(AttributeDef::getName)
        .doesNotContain(cursor);
  }
}
