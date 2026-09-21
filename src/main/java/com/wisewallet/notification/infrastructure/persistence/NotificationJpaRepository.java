package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.status = 'PENDING'
              AND n.channel = com.wisewallet.notification.domain.model.NotificationChannel.EMAIL
              AND n.createdAt < :cutoff
            ORDER BY n.createdAt ASC
            """)
    List<Notification> findPendingEmailOlderThan(@Param("cutoff") Instant cutoff);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = com.wisewallet.notification.domain.model.NotificationStatus.UNREAD")
    long countUnreadByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.status = com.wisewallet.notification.domain.model.NotificationStatus.READ,
                n.readAt = CURRENT_TIMESTAMP
            WHERE n.userId = :userId
              AND n.status = com.wisewallet.notification.domain.model.NotificationStatus.UNREAD
            """)
    int markAllReadByUserId(@Param("userId") UUID userId);
}
