package com.codeit.sb01otbooteam06.domain.dm.repository;

import com.codeit.sb01otbooteam06.domain.dm.entity.DirectMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DirectMessageCustomRepository {
    Page<DirectMessage> findLatestPerRoom(UUID userId, UUID cursor, Pageable pageable);
}