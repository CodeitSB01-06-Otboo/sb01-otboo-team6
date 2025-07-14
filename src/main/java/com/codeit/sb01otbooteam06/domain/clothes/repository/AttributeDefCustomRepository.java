package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import java.util.List;

public interface AttributeDefCustomRepository {

  List<AttributeDef> findAllByCursor(String cursor, String idAfter, int limit, String sortBy,
      String sortDirection, String keywordLike);

  int getTotalCounts(String sortBy, String keywordLike);
}
