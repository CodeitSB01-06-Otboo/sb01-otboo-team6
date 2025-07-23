package com.codeit.sb01otbooteam06.domain.notification.repository;

import com.codeit.sb01otbooteam06.domain.notification.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

}
