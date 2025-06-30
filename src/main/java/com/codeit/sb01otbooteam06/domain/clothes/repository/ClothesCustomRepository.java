package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.UUID;

public interface ClothesCustomRepository {

  List<Clothes> findAllByCursor(String cursor, String idAfter, int i, String typeEqual,
      UUID ownerId);

  int getTotalCounts(String typeEqual, UUID ownerId);
}
