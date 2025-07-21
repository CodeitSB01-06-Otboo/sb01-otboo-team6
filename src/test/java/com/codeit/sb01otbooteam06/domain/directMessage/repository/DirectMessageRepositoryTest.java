package com.codeit.sb01otbooteam06.domain.directMessage.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.sb01otbooteam06.domain.directMessage.TestFixture;
import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import com.codeit.sb01otbooteam06.domain.dm.repository.DirectMessageRepository;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
})
@Import(DirectMessageRepositoryTest.QuerydslTestConfig.class)   // ⬅️ 추가
class DirectMessageRepositoryTest {

    // QueryDSL 빈(테스트용)
    @TestConfiguration
    static class QuerydslTestConfig {
        @PersistenceContext
        EntityManager em;

        @Bean
        JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @Autowired
    DirectMessageRepository dmRepository;
    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("findLatestPerRoom()은 방마다 가장 최근 DM 한 건씩만 반환한다")
    void latestPerRoom() {
        // ── 사용자 ──
        User alice = persistUser("Alice");
        User bob   = persistUser("Bob");
        User carol = persistUser("Carol");

        Instant now = Instant.now();

        // Alice‑Bob 방: old → new (ID 자동 생성)
        DirectMessage oldAB = saveDm(alice, bob, "old",  now.minus(10, ChronoUnit.MINUTES));
        DirectMessage newAB = saveDm(alice, bob, "new",  now);               // 최신

        // Alice‑Carol 방: single
        DirectMessage onlyAC = saveDm(alice, carol, "single", now.minus(5, ChronoUnit.MINUTES));

        em.flush();
        em.clear();

        Page<DirectMessage> page = dmRepository.findLatestPerRoom(
            alice.getId(), null, PageRequest.of(0, 10));
        assertThat(page).hasSize(2);
        List<String> contents = page.stream()
            .map(DirectMessage::getContent)
            .toList();
        assertThat(contents).contains("single");
        // Alice–Bob 방 DM 은 "old" 또는 "new" 둘 중 하나
        assertThat(contents).anyMatch(c -> c.equals("old") || c.equals("new"));
    }

    // 헬퍼
    private User persistUser(String name) {
        User user = TestFixture.newUser(name);

        Instant now = Instant.now();

        //User 는 createdAt만
        ReflectionTestUtils.setField(user, "createdAt", now);

        // Profile 은 createdAt + updatedAt
        Profile profile = user.getProfile();
        ReflectionTestUtils.setField(profile, "createdAt", now);
        ReflectionTestUtils.setField(profile, "updatedAt", now);

        em.persist(user);          // cascade 로 profile 저장
        return user;
    }

    private DirectMessage saveDm(User s, User r, String txt, Instant when) {
        DirectMessage dm = DirectMessage.from(s, r, txt);
        ReflectionTestUtils.setField(dm, "createdAt", when);
        em.persist(dm);                 // ID는 자동으로 생성
        return dm;
    }

    private void setCreatedAt(DirectMessage dm, Instant time) {
        ReflectionTestUtils.setField(dm, "createdAt", time);
    }
}
