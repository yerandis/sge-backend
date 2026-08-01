package com.yerandis.sge.repository.notification;

import com.yerandis.sge.entity.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByReadAtIsNull();

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.readAt IS NULL")
    int markAllAsRead(LocalDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.id = :id AND n.readAt IS NULL")
    int markAsRead(UUID id, LocalDateTime now);
}
