package com.codeit.sb01otbooteam06.domain.user.repository;

import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver"
})
@Import(UserRepositoryTest.TestConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmail() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .name("테스트 유저")
                .role(Role.USER)
                .locked(false)
                .mustChangePassword(false)
                .build();

        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getCreatedAt()).isNotNull();  //  Auditing 확인
    }

    @Test
    @DisplayName("이메일 중복 여부 확인")
    void existsByEmail() {
        // given
        userRepository.save(User.builder()
                .email("dupe@example.com")
                .password("pass")
                .name("중복")
                .role(Role.USER)
                .locked(false)
                .mustChangePassword(false)
                .build());

        // when
        boolean exists = userRepository.existsByEmail("dupe@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("searchUsers - 이메일 필터로 사용자 검색")
    void searchUsers_withEmailLikeCondition() {
        // given
        userRepository.save(User.builder().email("alpha@example.com").password("p1").name("alpha").role(Role.USER).locked(false).mustChangePassword(false).build());
        userRepository.save(User.builder().email("beta@example.com").password("p2").name("beta").role(Role.USER).locked(false).mustChangePassword(false).build());
        userRepository.save(User.builder().email("gamma@example.com").password("p3").name("gamma").role(Role.USER).locked(false).mustChangePassword(false).build());

        // when
        List<User> result = userRepository.searchUsers(
                null,
                "alp",        // emailLike
                null,
                null,
                "email",
                "ASCENDING",
                10
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("alpha@example.com");
    }

    @TestConfiguration
    @EnableJpaAuditing  //  Auditing 활성화
    static class TestConfig {

        @Bean
        public AuditorAware<String> auditorAware() {
            return () -> Optional.of("test-user");
        }

        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }
    @Test
    @DisplayName("searchUsers - 잠금 여부로 사용자 필터링")
    void searchUsers_withLockedCondition() {
        userRepository.save(User.builder().email("lock1@example.com").password("p1").name("L1").role(Role.USER).locked(true).mustChangePassword(false).build());
        userRepository.save(User.builder().email("lock2@example.com").password("p2").name("L2").role(Role.USER).locked(false).mustChangePassword(false).build());

        List<User> result = userRepository.searchUsers(
                null,
                null,
                null,
                true,
                "email",
                "ASCENDING",
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isLocked()).isTrue();
    }

    @Test
    @DisplayName("searchUsers - 이메일 내림차순 정렬")
    void searchUsers_sortedByEmailDescending() {
        userRepository.save(User.builder()
                .email("b@example.com")
                .password("p1")
                .name("B")
                .role(Role.USER)
                .locked(false)
                .mustChangePassword(false)
                .build());

        userRepository.save(User.builder()
                .email("a@example.com")
                .password("p2")
                .name("A")
                .role(Role.USER)
                .locked(false)
                .mustChangePassword(false)
                .build());

        List<User> result = userRepository.searchUsers(
                null,
                null,
                null,
                null,  //  모든 locked 조건 제거
                "email",
                "DESCENDING",  //  내림차순
                10
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("b@example.com");
        assertThat(result.get(1).getEmail()).isEqualTo("a@example.com");
    }

    @Test
    @DisplayName("searchUsers - 역할로 필터링")
    void searchUsers_withRoleCondition() {
        userRepository.save(User.builder().email("admin@example.com").password("p1").name("admin").role(Role.ADMIN).locked(false).mustChangePassword(false).build());
        userRepository.save(User.builder().email("user@example.com").password("p2").name("user").role(Role.USER).locked(false).mustChangePassword(false).build());

        List<User> result = userRepository.searchUsers(
                null,
                null,
                Role.ADMIN.name(),
                null,
                "email",
                "ASCENDING",
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("searchUsers - createdAt 기준 정렬")
    void searchUsers_sortedByCreatedAt() {
        User user1 = userRepository.save(User.builder()
                .email("user1@example.com")
                .password("p")
                .name("a")
                .role(Role.USER)
                .locked(false)
                .mustChangePassword(false)
                .build());

        try {
            Thread.sleep(10); // createdAt 차이 유도
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태 복구
            throw new RuntimeException(e);      // 테스트 실패로 처리
        }

        User user2 = userRepository.save(User.builder()
                .email("user2@example.com")
                .password("p")
                .name("b")
                .role(Role.USER)
                .locked(false)
                .mustChangePassword(false)
                .build());

        List<User> result = userRepository.searchUsers(
                null,
                null,
                null,
                null,
                "createdAt",
                "DESCENDING",
                10
        );

        assertThat(result.get(0).getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    @DisplayName("searchUsers - role과 locked가 null이면 전체 반환")
    void searchUsers_nullRoleAndLocked() {
        userRepository.save(User.builder().email("user1@example.com").password("p").name("a").role(Role.ADMIN).locked(true).mustChangePassword(false).build());
        userRepository.save(User.builder().email("user2@example.com").password("p").name("b").role(Role.USER).locked(false).mustChangePassword(false).build());

        List<User> result = userRepository.searchUsers(
                null,
                null,
                null,  // roleEqual = null
                null,  // locked = null
                "email",
                "ASCENDING",
                10
        );

        assertThat(result).hasSize(2);
    }
}
