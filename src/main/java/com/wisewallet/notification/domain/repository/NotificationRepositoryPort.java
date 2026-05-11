package com.wisewallet.notification.domain.repository;

import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
    List<Notification> findPendingEmailOlderThan(Instant cutoff);
    long countUnreadByUserId(UUID userId);
    int markAllReadByUserId(UUID userId);
}
