package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QAttributeDef;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class AttributeDefCustomRepositoryImpl implements AttributeDefCustomRepository {


  private final JPAQueryFactory queryFactory;


  //todo" idAfter를 레포구현에서, 전반적 사용처 고민해보기 
  @Override
  public List<AttributeDef> findAllByCursor(String cursor, String idAfter, int limit, String sortBy,
      String sortDirection, String keywordLike) {

    QAttributeDef qAttributeDef = QAttributeDef.attributeDef;
    BooleanBuilder builder = new BooleanBuilder();

    /**
     * 1. .키워드 부분일치 검색
     * 2. 이름순 오름차순 정렬
     * 3. 이름 동일시 creatAt순으로 정렬
     * 4. 데이터를 limit만큼 컷
     */
    //키워드 검색- 부분일치 name & 선택가능값들
    if (keywordLike != null && !keywordLike.isEmpty()) {

      String likeExpr = "%" + keywordLike + "%";

      builder.and(
          qAttributeDef.name.likeIgnoreCase(likeExpr)
              .or(
                  Expressions.booleanTemplate(
                      "array_to_string({0}, ' ') ILIKE {1}",
                      qAttributeDef.selectableValues,
                      likeExpr
                  )
              )
      );
    } else {
      builder.and(qAttributeDef.isNotNull());
    }

    // 정렬 조건
    OrderSpecifier<?> orderSpecifier; //주정렬조건
    OrderSpecifier<?> secondaryOrderSpecifier; //

    if (sortDirection.equals("ASCENDING")) {
      orderSpecifier = qAttributeDef.name.asc();
    } else {
      orderSpecifier = qAttributeDef.name.desc();
    }

    //커서 존재시 페이지네이션 적용
    if (cursor != null && !cursor.isEmpty()) {
      if (sortDirection.equals("ASCENDING")) {
        builder.and(qAttributeDef.name.gt(cursor));
      } else {
        builder.and(qAttributeDef.name.lt(cursor));
      }
    }

    // 보조 정렬 조건으로 createdAt 추가
    secondaryOrderSpecifier =
        sortDirection.equalsIgnoreCase("ASCENDING") ? qAttributeDef.createdAt.asc()
            : qAttributeDef.createdAt.desc();

    List<AttributeDef> attributeDefs = queryFactory.selectFrom(qAttributeDef)
        .where(builder)
        .orderBy(orderSpecifier, secondaryOrderSpecifier)
        .limit(limit)
        .fetch();

    return attributeDefs;


  }

  @Override
  public int getTotalCounts(String sortBy, String keywordLike) {
    QAttributeDef qAttributeDef = QAttributeDef.attributeDef;
    BooleanBuilder builder = new BooleanBuilder();

    // 부분 일치 검색
    if (keywordLike != null && !keywordLike.isEmpty()) {

      String likeExpr = "%" + keywordLike + "%";

      builder.and(
          qAttributeDef.name.likeIgnoreCase(likeExpr)
              .or(
                  Expressions.booleanTemplate(
                      "array_to_string({0}, ' ') ILIKE {1}",
                      qAttributeDef.selectableValues,
                      likeExpr
                  )
              )
      );
    } else {
      builder.and(qAttributeDef.isNotNull());
    }

    return Math.toIntExact(queryFactory
        .select(qAttributeDef.count())
        .from(qAttributeDef)
        .where(builder)
        .fetchOne());

  }
}
