package com.codeit.sb01otbooteam06.domain.dm.repository;

import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import com.codeit.sb01otbooteam06.domain.dm.entity.QDirectMessage;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Slf4j
@RequiredArgsConstructor
public class DirectMessageCustomRepositoryImpl implements DirectMessageCustomRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<DirectMessage> findLatestPerRoom(UUID userId,
        UUID cursor,
        Pageable pageable) {

        QDirectMessage dm    = QDirectMessage.directMessage;
        QDirectMessage dmSub = new QDirectMessage("dmSub");

        Expression<String> dmKeySub = Expressions.stringTemplate(
            "CASE WHEN {0} < {1} THEN CONCAT({0}, '_', {1}) ELSE CONCAT({1}, '_', {0}) END",
            dmSub.sender.id, dmSub.receiver.id);

        // ── 각 방의 최신 id
        JPQLQuery<UUID> maxIdSub = JPAExpressions
            .select(dmSub.id.max())
            .from(dmSub)
            .where(dmSub.sender.id.eq(userId)
                .or(dmSub.receiver.id.eq(userId)))
            .groupBy(dmKeySub);

        // ── 메인 쿼리
        List<DirectMessage> content = query
            .selectFrom(dm)
            .where(dm.id.in(maxIdSub))
            .where(cursor != null ? dm.id.lt(cursor) : null)
            .orderBy(dm.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        log.debug("[QDSL] latestPerRoom user={}, cursor={}, page={}, ret={}",
            userId, cursor, pageable, content.size());

        return new PageImpl<>(content, pageable, content.size());
    }
}
