package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QAttributeDef;
import com.codeit.sb01otbooteam06.domain.clothes.entity.QClothesAttribute;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothesAttributeCustomRepositoryImpl implements ClothesAttributeCustomRepository {

  private final JPAQueryFactory queryFactory;


  /**
   * 의상 id 리스트에 대한 의상속성 테이블 값을 한번에 로드
   *
   * @param clothesIds 조회할 의상들의 ID 리스트
   * @return 해당 의상들의 의상 속성 목록
   */
  @Override
  public List<ClothesAttribute> findAttributesByClothesIds(List<UUID> clothesIds) {
    if (clothesIds == null || clothesIds.isEmpty()) {
      return Collections.emptyList();
    }
    
    QClothesAttribute qClothesAttribute = QClothesAttribute.clothesAttribute;
    QAttributeDef qAttributeDef = QAttributeDef.attributeDef;

    return queryFactory
        .selectFrom(qClothesAttribute)
        .join(qClothesAttribute.attributeDef, qAttributeDef).fetchJoin()
        .where(qClothesAttribute.clothes.id.in(clothesIds))
        .fetch();
  }
}
