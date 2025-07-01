package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ClothesCustomRepositoryImpl implements ClothesCustomRepository {

  @Override
  public List<Clothes> findAllByCursor(String cursor, String idAfter, int i, String typeEqual,
      UUID ownerId) {
    return List.of();
  }

  @Override
  public int getTotalCounts(String typeEqual, UUID ownerId) {
    return 0;
  }
}
