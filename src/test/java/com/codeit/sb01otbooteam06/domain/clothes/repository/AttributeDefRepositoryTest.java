//package com.codeit.sb01otbooteam06.domain.clothes.repository;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//import com.codeit.sb01otbooteam06.config.JpaAuditingConfiguration;
//import com.codeit.sb01otbooteam06.config.QueryDslConfig;
//import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
//import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
//import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
//import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
//import com.codeit.sb01otbooteam06.domain.user.entity.User;
//import com.codeit.sb01otbooteam06.util.EntityProvider;
//import jakarta.transaction.Transactional;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//
//@DataJpaTest
//@ActiveProfiles("test")
//@Transactional
//@Import({QueryDslConfig.class, JpaAuditingConfiguration.class})
//@TestPropertySource(properties = {
//    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
//    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
//    "spring.sql.init.mode=never",
//    "spring.jpa.hibernate.ddl-auto=create-drop"
//})
//public class AttributeDefRepositoryTest {
//
//  @Autowired
//  private TestEntityManager entityManager; // 엔티티를 저장 및 조회할때마다 영속성 컨텍스트에 엔티티를 보관하고 관리한다.
//
//  @Autowired
//  private AttributeDefRepository attributeDefRepository;
//
//  // 클래스 필드 선언만
//  static String defName = "계절";
//  static List<String> defValues = List.of("봄", "여름", "가을", "계절");
//
//  User user;
//  Profile profile;
//  AttributeDef attributeDef;
//  Clothes clothes;
//  ClothesAttribute clothesAttribute;
//
//  @BeforeEach
//  void setUp() {
//    user = EntityProvider.createTestUser();
//    profile = EntityProvider.createTestProfile(user);
//    clothes = EntityProvider.createTestClothes(user);
//    attributeDef = EntityProvider.createTestAttributeDef(defName, defValues);
//    clothesAttribute = EntityProvider.createTestClothesAttribute(clothes, attributeDef, "봄");
//
//    entityManager.persist(user);
//    entityManager.persist(profile);
//    entityManager.persist(attributeDef);
//    entityManager.persist(clothes);
//    entityManager.persist(clothesAttribute);
//    entityManager.clear();
//  }
//
//
//  @Nested
//  @DisplayName("커서 기반 속성 정의 조회")
//  public class FindAllByCursorTest {
//
//    @BeforeEach
//    void setUp() {
//
//    }
//
//    @Test
//    public void findAllByCursorTestSuccess() {
//
//    }
//  }
//
//  @Test
//  void getTotalCounts_키워드없을때_전체카운트확인() {
//    int totalCount = attributeDefRepository.getTotalCounts("name", null);
//    assertThat(totalCount).isEqualTo(1);
//  }
//
//
//}
