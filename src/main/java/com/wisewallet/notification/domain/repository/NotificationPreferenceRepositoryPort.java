package com.wisewallet.notification.domain.repository;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepositoryPort {
    NotificationPreference save(NotificationPreference preference);
    List<NotificationPreference> findByUserId(UUID userId);
    Optional<NotificationPreference> findByUserIdAndEventTypeAndChannel(UUID userId, EventType eventType, NotificationChannel channel);
    List<NotificationPreference> findByUserIdAndEventType(UUID userId, EventType eventType);
}
