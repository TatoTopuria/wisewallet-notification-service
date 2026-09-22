package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.application.port.out.NotificationQueryRepositoryPort;
import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort, NotificationQueryRepositoryPort {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Notification> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public List<Notification> findPendingEmailOlderThan(Instant cutoff) {
        return jpaRepository.findPendingEmailOlderThan(cutoff);
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countUnreadByUserId(userId);
    }

    @Override
    public int markAllReadByUserId(UUID userId) {
        return jpaRepository.markAllReadByUserId(userId);
    }

    public Page<Notification> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable);
    }
}
