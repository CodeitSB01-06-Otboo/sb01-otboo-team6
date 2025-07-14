package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.user.entity.User;
import java.util.List;
import java.util.UUID;

public interface ClothesCustomRepository {

  List<Clothes> findAllByCursor(String cursor, String idAfter, int limit, String typeEqual,
      UUID ownerId);

  int getTotalCounts(String typeEqual, UUID ownerId);

  List<Clothes> findAllByOwnerWithValue(User user, int[] weightData);
}
