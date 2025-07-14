package com.codeit.sb01otbooteam06.domain.clothes.repository;

import com.codeit.sb01otbooteam06.domain.clothes.entity.AttributeDef;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeDefRepository extends JpaRepository<AttributeDef, UUID>,
    AttributeDefCustomRepository {

  boolean existsByName(String name);
}
