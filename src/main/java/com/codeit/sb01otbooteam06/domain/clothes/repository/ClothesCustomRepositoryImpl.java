package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QAttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QClothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QClothesAttribute;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository {

  private final JPAQueryFactory queryFactory;

  /**
   * 검색 조건에 따른 의상 목록을 반환합니다.
   *
   * @param cursor
   * @param idAfter
   * @param limit
   * @param typeEqual
   * @param ownerId
   * @return 의상 목록
   */
  @Override
  public List<Clothes> findAllByCursor(String cursor, String idAfter, int limit, String typeEqual,
      UUID ownerId) {
    QClothes qClothes = QClothes.clothes;
    BooleanBuilder builder = new BooleanBuilder();

    //type, ownerId 1차 select
    builder.and(qClothes.type.eq(typeEqual));
    builder.and(qClothes.owner.id.eq(ownerId));

    //정렬: createAt 내림차순
    OrderSpecifier<?> orderSpecifier;
    orderSpecifier = qClothes.createdAt.desc();

    //커서존재할경우
    if (cursor != null && idAfter != null) {
      // 내림차순 정렬
      builder.and(qClothes.createdAt.lt(Instant.parse(cursor)));

    }

    List<Clothes> clothesList = queryFactory
        .selectFrom(qClothes)
        .where(builder)
        .orderBy(orderSpecifier)
        .limit(limit)
        .fetch();

    return clothesList;
  }


  /**
   * 사용자의 의상 타입에 따른 의상 개수를 반환한다.
   *
   * @param typeEqual
   * @param ownerId
   * @return 사용자의 의상 타입에 따른 의상 개수
   */
  @Override
  public int getTotalCounts(String typeEqual, UUID ownerId) {
    QClothes qClothes = QClothes.clothes;
    BooleanBuilder builder = new BooleanBuilder();

    //검색 조건
    builder.and(qClothes.type.eq(typeEqual));
    builder.and(qClothes.owner.id.eq(ownerId));

    return Math.toIntExact(queryFactory
        .select(qClothes.count())
        .from(qClothes)
        .where(builder)
        .fetchOne());

  }

  /**
   * 의상 추천에서, 속성 값에 따른 유저의 의상을 반환한다.
   *
   * @param user
   * @param weightData
   * @return
   */
  @Override
  public List<Clothes> findAllByOwnerWithValue(User user, int[] weightData) {
    QClothes qClothes = QClothes.clothes;
    QClothesAttribute qClothesAttribute = QClothesAttribute.clothesAttribute;
    QAttributeDef qAttributeDef = QAttributeDef.attributeDef;

    // 속성 이름과 기대값 매핑 (DB에서 조회)
    Map<String, String> expectedAttributes = new HashMap<>();
    String[] attributeNames = {"계절", "두께감", "안감", "따뜻한 정도"};

    for (int i = 0; i < attributeNames.length; i++) {
      String attributeName = attributeNames[i];
      Integer index = weightData[i];

      // attributes_defs 테이블에서 해당 속성의 selectable_values 조회
      AttributeDef attributeDef = queryFactory
          .selectFrom(qAttributeDef)
          .where(qAttributeDef.name.eq(attributeName))
          .fetchOne();

      if (attributeDef != null && attributeDef.getSelectableValues() != null) {
        List<String> selectableValues = attributeDef.getSelectableValues();
        if (index < selectableValues.size()) {
          expectedAttributes.put(attributeName, selectableValues.get(index));
        }
      }
    }

    // 조건식들을 OR로 연결 (하나라도 일치하지 않으면 제외)
    BooleanExpression mismatchCondition = null;

    for (Map.Entry<String, String> entry : expectedAttributes.entrySet()) {
      BooleanExpression attrMismatch = qAttributeDef.name.eq(entry.getKey())
          .and(qClothesAttribute.value.ne(entry.getValue()));

      if (mismatchCondition == null) {
        mismatchCondition = attrMismatch;
      } else {
        mismatchCondition = mismatchCondition.or(attrMismatch);
      }
    }

    // 불일치하는 속성을 가진 의상 ID들
    JPQLQuery<UUID> invalidClothesIds = JPAExpressions
        .select(qClothesAttribute.clothes.id)
        .from(qClothesAttribute)
        .join(qAttributeDef).on(qClothesAttribute.attributeDef.eq(qAttributeDef))
        .where(mismatchCondition);

    // 최종 결과 반환
    return queryFactory
        .selectFrom(qClothes)
        .where(
            qClothes.owner.eq(user)
                .and(qClothes.id.notIn(invalidClothesIds))
        )
        .fetch();
  }
}
