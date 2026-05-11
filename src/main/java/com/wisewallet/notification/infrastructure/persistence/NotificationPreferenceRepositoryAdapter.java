package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.domain.repository.NotificationPreferenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationPreferenceRepositoryAdapter implements NotificationPreferenceRepositoryPort {

    private final NotificationPreferenceJpaRepository jpaRepository;

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        return jpaRepository.save(preference);
    }

    @Override
    public List<NotificationPreference> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<NotificationPreference> findByUserIdAndEventTypeAndChannel(UUID userId, EventType eventType, NotificationChannel channel) {
        return jpaRepository.findByUserIdAndEventTypeAndChannel(userId, eventType, channel);
    }

    @Override
    public List<NotificationPreference> findByUserIdAndEventType(UUID userId, EventType eventType) {
        return jpaRepository.findByUserIdAndEventType(userId, eventType);
    }
}
