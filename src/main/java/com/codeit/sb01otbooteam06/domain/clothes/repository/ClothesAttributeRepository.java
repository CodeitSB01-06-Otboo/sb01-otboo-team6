package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.Clothes;
import com.codeit.sb01otbooteam06.domain.clothes.entity.ClothesAttribute;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {

  void deleteByClothes(Clothes clothes);

  List<ClothesAttribute> findByClothes(Clothes clothes);

  List<ClothesAttribute> findByClothesIn(List<Clothes> clothesList);
}
