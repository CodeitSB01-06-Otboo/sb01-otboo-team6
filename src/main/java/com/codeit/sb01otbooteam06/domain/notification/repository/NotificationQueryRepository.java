package com.codeit.sb01otbooteam06.domain.notification.repository;

import autovalue.shaded.org.checkerframework.checker.nullness.qual.Nullable;
import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import com.codeit.sb01otbooteam06.domain.notification.entity.QNotification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

  private final JPAQueryFactory queryFactory;

  public List<Notification> findUnreadByUserIdWithCursorPagination(
      UUID userId,
      @Nullable Instant cursorCreatedAt,
      @Nullable UUID cursorId,
      int limit
  ) {
    QNotification n = QNotification.notification;

    return queryFactory
        .selectFrom(n)
        .where(
            n.user.id.eq(userId),
            n.isRead.isFalse(),
            cursorCondition(cursorCreatedAt, cursorId, n)
        )
        .orderBy(n.createdAt.desc(), n.id.desc())
        .limit(limit)
        .fetch();
  }

  private BooleanExpression cursorCondition(
      @Nullable Instant cursorCreatedAt,
      @Nullable UUID cursorId,
      QNotification n
  ) {
    if (cursorCreatedAt == null || cursorId == null) {
      return null;
    }

    return n.createdAt.lt(cursorCreatedAt)
        .or(n.createdAt.eq(cursorCreatedAt).and(n.id.lt(cursorId)));
  }

}
