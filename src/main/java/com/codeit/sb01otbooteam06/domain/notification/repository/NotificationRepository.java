package com.codeit.sb01otbooteam06.domain.notification.repository;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  int countByUserIdAndIsReadFalse(UUID userId);

}
