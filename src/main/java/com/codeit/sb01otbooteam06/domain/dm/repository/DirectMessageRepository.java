package com.codeit.sb01otbooteam06.domain.dm.repository;

import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID>,DirectMessageCustomRepository {

    @Query("""
          SELECT dm FROM DirectMessage dm
           WHERE ((dm.sender.id = :me AND dm.receiver.id = :you)
               OR (dm.sender.id = :you AND dm.receiver.id = :me))
             AND (:cursor IS NULL OR dm.id < :cursor)
        ORDER BY dm.id DESC
        """)
    List<DirectMessage> findChat(@Param("me") UUID me,
        @Param("you") UUID you,
        @Param("cursor") UUID cursor,
        Pageable pageable);
}
