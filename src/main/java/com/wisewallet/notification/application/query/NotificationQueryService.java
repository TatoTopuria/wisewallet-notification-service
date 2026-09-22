package com.wisewallet.notification.application.query;

import com.wisewallet.notification.application.port.out.NotificationQueryRepositoryPort;
import com.wisewallet.notification.domain.exception.NotificationNotFoundException;
import com.wisewallet.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationQueryRepositoryPort notificationRepository;

    @Transactional(readOnly = true)
    public Page<Notification> listNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Notification getNotification(UUID id, UUID userId) {
        return notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}
