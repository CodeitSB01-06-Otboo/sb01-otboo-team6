package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import java.util.List;
import java.util.UUID;

public interface ClothesAttributeCustomRepository {

  List<ClothesAttribute> findAttributesByClothesIds(List<UUID> clothesIds);
}
