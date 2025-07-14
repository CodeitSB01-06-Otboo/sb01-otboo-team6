package com.codeit.sb01otbooteam06.domain.feed.repository;

import com.codeit.sb01otbooteam06.domain.feed.entity.Feed;
import com.codeit.sb01otbooteam06.domain.feed.entity.QFeed;
import com.codeit.sb01otbooteam06.domain.weather.entity.PrecipitationType;
import com.codeit.sb01otbooteam06.domain.weather.entity.QWeather;
import com.codeit.sb01otbooteam06.domain.weather.entity.SkyStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {

  private final JPAQueryFactory queryFactory;

  private final QFeed feed = QFeed.feed;
  private final QWeather weather = QWeather.weather;

  @Override
  public Page<Feed> findFeedsByCursorAndSort(
      String keyword,
      SkyStatus skyStatus,
      PrecipitationType precipitationType,
      Object cursorValue,
      UUID cursorId,
      Pageable pageable
  ) {
    BooleanBuilder condition = new BooleanBuilder();

    // 필터 조건
    if (keyword != null) {
      condition.and(feed.content.lower().like("%" + keyword.toLowerCase() + "%"));
    }
    if (skyStatus != null) {
      condition.and(feed.weather.skyStatus.eq(skyStatus));
    }
    if (precipitationType != null) {
      condition.and(feed.weather.precipitationType.eq(precipitationType));
    }

    // 정렬 기준: createdAt 또는 likeCount
    Sort.Order sortOrder = pageable.getSort().getOrderFor("likeCount") != null
        ? pageable.getSort().getOrderFor("likeCount")
        : pageable.getSort().getOrderFor("createdAt");

    if (sortOrder == null) {
      throw new IllegalArgumentException("정렬 기준(createdAt 또는 likeCount)을 지정해야 합니다.");
    }

    OrderSpecifier<?> primaryOrder;
    OrderSpecifier<UUID> secondaryOrder = feed.id.desc();

    if (sortOrder.getProperty().equals("likeCount")) {
      primaryOrder = sortOrder.isAscending() ? feed.likeCount.asc() : feed.likeCount.desc();
      if (cursorValue != null && cursorId != null) {
        condition.and(
            feed.likeCount.lt((Long) cursorValue)
                .or(feed.likeCount.eq((Long) cursorValue).and(feed.id.lt(cursorId)))
        );
      }
    } else { // createdAt
      primaryOrder = sortOrder.isAscending() ? feed.createdAt.asc() : feed.createdAt.desc();
      if (cursorValue != null && cursorId != null) {
        condition.and(
            feed.createdAt.lt((Instant) cursorValue)
                .or(feed.createdAt.eq((Instant) cursorValue).and(feed.id.lt(cursorId)))
        );
      }
    }

    List<Feed> content = queryFactory.selectFrom(feed)
        .join(feed.weather, weather).fetchJoin()
        .where(condition)
        .orderBy(primaryOrder, secondaryOrder)
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = content.size() > pageable.getPageSize();
    if (hasNext) content.remove(pageable.getPageSize());

    return new PageImpl<>(content, pageable, hasNext ? pageable.getOffset() + content.size() + 1 : pageable.getOffset() + content.size());
  }

  @Override
  public long countByFilters(String keyword, SkyStatus skyStatus, PrecipitationType precipitationType) {
    BooleanBuilder condition = new BooleanBuilder();

    if (keyword != null) {
      condition.and(feed.content.lower().like("%" + keyword.toLowerCase() + "%"));
    }
    if (skyStatus != null) {
      condition.and(feed.weather.skyStatus.eq(skyStatus));
    }
    if (precipitationType != null) {
      condition.and(feed.weather.precipitationType.eq(precipitationType));
    }

    return queryFactory.select(feed.count())
        .from(feed)
        .join(feed.weather, weather)
        .where(condition)
        .fetchOne();
  }
}
