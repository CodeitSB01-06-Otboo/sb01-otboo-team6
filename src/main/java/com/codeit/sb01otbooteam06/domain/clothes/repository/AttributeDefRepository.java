package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeDefRepository extends JpaRepository<AttributeDef, UUID>,
    AttributeDefCustomRepository {

  boolean existsByName(String name);


  @Query("SELECT a.selectableValues FROM AttributeDef a WHERE a.name = :name")
  List<String> findSelectableValuesByName(@Param("name") String name);
}
