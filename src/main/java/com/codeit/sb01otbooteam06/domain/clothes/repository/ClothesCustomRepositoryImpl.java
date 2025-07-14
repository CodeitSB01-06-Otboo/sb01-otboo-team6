package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QClothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
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
//    //weightData = [계절, 두께감, 안감, 따뜻한 정도]
//    QClothes qClothes = QClothes.clothes;
//    QClothesAttribute qClothesAttribute = QClothesAttribute.clothesAttribute;
//    QAttributeDef qAttributeDef = QAttributeDef.attributeDef;
//
//// 속성 이름과 weightData 인덱스 매핑
//    Map<String, Integer> attrIndexMap = Map.of(
//        "계절", weightData[0],
//        "두께감", weightData[1],
//        "안감", weightData[2],
//        "따뜻한 정도", weightData[3]
//    );
//
//    // 속성 이름 → 일치해야 하는 값
//    Map<String, String> nameToExpectedValue = attrIndexMap.entrySet().stream()
//        .collect(Collectors.toMap(
//            Map.Entry::getKey,
//            e -> getSelectableValuesForAttribute(e.getKey())[e.getValue()]
//        ));
//
//    // 조건: 존재하는 속성 중에서 일치하지 않는 항목이 하나라도 있으면 제외
//    JPQLQuery<UUID> invalidClothesIds = JPAExpressions
//        .select(qClothesAttribute.clothes.id)
//        .from(qClothesAttribute)
//        .join(qAttributeDef).on(qClothesAttribute.attributeDef.eq(qAttributeDef))
//        .where(
//            qClothesAttribute.attributeDef.name.in(nameToExpectedValue.keySet())
//                .and(qClothesAttribute.value.neAll( // 존재하는 속성이 기대값이 아닐 경우
//                    nameToExpectedValue.entrySet().stream()
//                        .map(Map.Entry::getValue)
//                        .toArray(String[]::new)
//                ))
//        );
//
//    // 조건: 소유자 일치 & 불일치 항목이 없는 의상
//    return queryFactory
//        .selectFrom(qClothes)
//        .where(
//            qClothes.owner.eq(user)
//                .and(qClothes.id.notIn(invalidClothesIds))
//        )
//        .fetch();

    return List.of();
  }
}
