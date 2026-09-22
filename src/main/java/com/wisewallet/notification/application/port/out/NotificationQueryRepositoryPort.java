package com.wisewallet.notification.application.port.out;

import com.wisewallet.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface NotificationQueryRepositoryPort {
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    long countUnreadByUserId(UUID userId);
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
