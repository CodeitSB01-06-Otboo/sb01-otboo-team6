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


  @Query("""
    SELECT n FROM Notification n
    WHERE n.user.id = :userId
      AND n.isRead = false
      AND (
        :cursorCreatedAt IS NULL OR
        (n.createdAt < :cursorCreatedAt OR
         (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))
      )
    ORDER BY n.createdAt DESC, n.id DESC
""")
  List<Notification> findUnreadByUserIdWithCursorPagination(
      @Param("userId") UUID userId,
      @Param("cursorCreatedAt") Instant cursorCreatedAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable
  );

  int countByUserIdAndReadFalse(UUID userId);

}
