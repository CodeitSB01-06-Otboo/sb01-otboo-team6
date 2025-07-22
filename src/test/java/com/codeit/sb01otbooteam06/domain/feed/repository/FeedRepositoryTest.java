package com.codeit.sb01otbooteam06.domain.feed.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.user.entity.Role;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.codeit.sb01otbooteam06.domain.weather.entity.Location;
import com.codeit.sb01otbooteam06.domain.weather.entity.Precipitation;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.codeit.sb01otbooteam06.domain.weather.entity.Temperature;
import com.codeit.sb01otbooteam06.domain.weather.entity.Weather;
import com.codeit.sb01otbooteam06.domain.weather.entity.Wind;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
})
@EnableJpaAuditing
@Import(FeedRepositoryTest.QuerydslTestConfig.class)
public class FeedRepositoryTest {


  @TestConfiguration
  static class QuerydslTestConfig {

    @PersistenceContext
    EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(em);
    }

    @Bean
    public FeedQueryRepository feedQueryRepository(JPAQueryFactory jpaQueryFactory) {
      return new FeedQueryRepositoryImpl(jpaQueryFactory);
    }
  }

  @Autowired
  TestEntityManager em;


  @Autowired
  FeedQueryRepository feedQueryRepository;

  Feed feed1, feed2, feed3;

  @BeforeEach
  void setUp() {
    // 위치 객체
    Location location = Location.from(33.5, 126.5, 55, 127);

    // 날씨 엔티티 생성
    Weather weather1 = Weather.from(
        Instant.now().minusSeconds(600),
        Instant.now().plusSeconds(3600),
        location
    );
    weather1.applyMetrics(
        SkyStatus.CLEAR,
        PrecipitationType.RAIN,
        Temperature.from(28.0, 23.0, 30.0),
        Precipitation.from(1.0, "보통", 60.0),
        Wind.from(3.2, 2, 0.5, 0.1, 0.1),
        85.0,
        0.5,
        0.0
    );
    em.persist(weather1);

    Weather weather2 = Weather.from(
        Instant.now().minusSeconds(600),
        Instant.now().plusSeconds(3600),
        location
    );
    weather2.applyMetrics(
        SkyStatus.CLOUDY,
        PrecipitationType.DRIZZLE_SNOW,
        Temperature.from(28.0, 23.0, 30.0),
        Precipitation.from(1.0, "보통", 60.0),
        Wind.from(3.2, 2, 0.5, 0.1, 0.1),
        85.0,
        0.5,
        0.0
    );
    em.persist(weather2);

    weather2.applyMetrics(
        SkyStatus.CLOUDY,
        PrecipitationType.DRIZZLE_SNOW,
        Temperature.from(28.0, 23.0, 30.0),
        Precipitation.from(1.0, "보통", 60.0),
        Wind.from(3.2, 2, 0.5, 0.1, 0.1),
        85.0,
        0.5,
        0.0
    );
    em.persist(weather2);

    User user = User.builder()
        .name("test")
        .email("test@test.com")
        .role(Role.USER)
        .password("password")
        .build();
    em.persist(user);

    // 피드 생성
    feed1 = Feed.of("비 오는 날의 코디 추천", user, weather1);
    feed1.like();
    em.persist(feed1);

    feed2 = Feed.of("맑은 날씨엔 이렇게 입어요", user, weather2);
    feed2.like();
    feed2.like();
    em.persist(feed2);

    feed3 = Feed.of("늦게 생성된 피드", user, weather2);
    feed3.like(); // 좋아요 1개
    em.persist(feed3);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("likeCount 내림차순 커서 페이지네이션 테스트")
  void findFeedsByLikeCountCursorDesc() {
    Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("likeCount")));
    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        feed2.getLikeCount(), feed2.getId(),
        pageable
    );

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(feed1.getId());
  }

  @Test
  @DisplayName("createdAt 내림차순 커서 페이지네이션 테스트")
  void findFeedsByCreatedAtCursorDesc() {
    Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdAt")));
    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        feed2.getCreatedAt(), feed2.getId(),
        pageable
    );

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(feed1.getId());
  }

  @Test
  @DisplayName("keyword, skyStatus, precipitationType 필터 조건으로 count 조회")
  void countByFilters_with_conditions() {
    long count = feedQueryRepository.countByFilters("비", SkyStatus.CLEAR, PrecipitationType.RAIN);
    assertThat(count).isEqualTo(1);
  }

  @Test
  @DisplayName("필터 조건 없이 전체 count 조회")
  void countByFilters_no_conditions() {
    long count = feedQueryRepository.countByFilters(null, null, null);
    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("keyword 필터가 적용되는 경우")
  void findFeeds_withKeywordFilter() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        "코디",
        null,
        null,
        null,
        null,
        pageable
    );

    assertThat(result.getContent()).hasSize(1); // feed1만 "코디" 포함
    assertThat(result.getContent().get(0).getContent()).contains("코디");
  }

  @Test
  @DisplayName("skyStatus 필터가 적용되는 경우")
  void findFeeds_withSkyStatusFilter() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        SkyStatus.CLOUDY,
        null,
        null,
        null,
        pageable
    );

    assertThat(result.getContent()).hasSize(1); // feed2만 CLOUDY
    assertThat(result.getContent().get(0).getWeather().getSkyStatus()).isEqualTo(SkyStatus.CLOUDY);
  }

  @Test
  @DisplayName("precipitationType 필터가 적용되는 경우")
  void findFeeds_withPrecipitationTypeFilter() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        PrecipitationType.DRIZZLE_SNOW,
        null,
        null,
        pageable
    );

    assertThat(result.getContent()).hasSize(1); // feed2만 DRIZZLE_SNOW
    assertThat(result.getContent().get(0).getWeather().getPrecipitationType()).isEqualTo(PrecipitationType.DRIZZLE_SNOW);
  }

  @Test
  @DisplayName("정렬 기준이 likeCount가 아닌 createdAt일 때 동작 확인")
  void findFeeds_sortedByCreatedAt() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        null,
        null,
        pageable
    );

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getCreatedAt()).isAfter(result.getContent().get(1).getCreatedAt());
  }

  @Test
  @DisplayName("정렬 기준이 없는 경우 예외 발생")
  void findFeeds_withoutSort_throwsException() {
    Pageable pageable = PageRequest.of(0, 10);

    assertThatThrownBy(() ->
        feedQueryRepository.findFeedsByCursorAndSort(
            null, null, null,
            null, null,
            pageable
        )
    )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("정렬 기준");
  }

  @Test
  @DisplayName("likeCount 정렬 기준 + cursorValue와 cursorId가 있는 경우")
  void findFeeds_sortedByLikeCount_withCursor() {
    Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("likeCount")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        10L,
        feed2.getId(),
        pageable
    );

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("createdAt 정렬 기준 + cursorValue와 cursorId가 있는 경우")
  void findFeeds_sortedByCreatedAt_withCursor() {
    Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        feed2.getCreatedAt(),
        feed2.getId(),
        pageable
    );

    assertThat(result.getContent()).hasSize(1); // feed1만 조회
    assertThat(result.getContent().get(0).getId()).isEqualTo(feed1.getId());
  }

  @Test
  @DisplayName("likeCount 정렬 기준 + cursor 없이 전체 조회")
  void findFeeds_sortedByLikeCount_withoutCursor() {
    Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("likeCount")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        null,
        null,
        pageable
    );

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getLikeCount()).isGreaterThanOrEqualTo(
        result.getContent().get(1).getLikeCount()
    );
  }

  @Test
  @DisplayName("createdAt 정렬 기준 + cursor 없이 전체 조회")
  void findFeeds_sortedByCreatedAt_withoutCursor() {
    Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        null, null,
        pageable
    );

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getCreatedAt()).isAfter(
        result.getContent().get(1).getCreatedAt()
    );
  }

  @Test
  @DisplayName("createdAt 정렬 기준 + cursorValue만 있는 경우 (cursorId는 null)")
  void findFeeds_sortedByCreatedAt_onlyCursorValue() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("createdAt")));

    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null,
        null,
        null,
        feed2.getCreatedAt(),
        null, // cursorId 없음
        pageable
    );

    assertThat(result.getContent())
        .extracting("id")
        .containsExactly(feed1.getId()); // feed1.createdAt < feed2.createdAt
  }

  @Test
  @DisplayName("likeCount 정렬 기준 ASC + cursorValue만 있는 경우 (cursorId는 null)")
  void findFeeds_sortedByLikeCountAsc_onlyCursorValue() {
    // given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("likeCount")));

    Long cursorLikeCount = feed1.getLikeCount(); // feed1 = 5

    // when
    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null, null, null,
        cursorLikeCount, // cursorValue만 존재
        null,            // cursorId 없음
        pageable
    );

    // then
    assertThat(result.getContent())
        .extracting("id")
        .containsExactly(feed2.getId()); // feed2.likeCount = 10
  }

  @Test
  @DisplayName("createdAt 정렬 기준 ASC + cursorValue만 있는 경우 (cursorId는 null)")
  void findFeeds_sortedByCreatedAtAsc_onlyCursorValue() {
    // given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdAt")));

    Instant cursorCreatedAt = feed1.getCreatedAt(); // feed1은 오래된 피드

    // when
    Page<Feed> result = feedQueryRepository.findFeedsByCursorAndSort(
        null, null, null,
        cursorCreatedAt, // cursorValue만 존재
        null,            // cursorId 없음
        pageable
    );

    // then
    assertThat(result.getContent())
        .extracting("id")
        .containsExactly(feed2.getId(), feed3.getId()); // feed2, feed3은 feed1보다 이후에 생성
  }
}





