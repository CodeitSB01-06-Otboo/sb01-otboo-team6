package com.codeit.sb01otbooteam06.domain.profile.repository;

import com.codeit.sb01otbooteam06.domain.profile.entity.Gender;
import com.codeit.sb01otbooteam06.domain.profile.entity.Profile;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver"
})
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Profile 저장 및 조회 테스트")
    void saveAndFindProfile() {

        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        Profile profile = Profile.builder()
                .user(user)
                .name("테스트")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .temperatureSensitivity(0)
                .build();
        profileRepository.save(profile);


        Profile found = profileRepository.findById(profile.getId()).orElseThrow();


        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getName()).isEqualTo("테스트");
    }

    @TestConfiguration
    static class TestQueryDslConfig {
        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }
}
