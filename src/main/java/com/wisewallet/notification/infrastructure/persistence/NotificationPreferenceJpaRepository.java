package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByUserId(UUID userId);

    Optional<NotificationPreference> findByUserIdAndEventTypeAndChannel(UUID userId, EventType eventType, NotificationChannel channel);

    List<NotificationPreference> findByUserIdAndEventType(UUID userId, EventType eventType);
}
