package com.wisewallet.notification.application.command;

import com.wisewallet.notification.application.query.PreferenceQueryService;
import com.wisewallet.notification.application.shared.IdempotencyChecker;
import com.wisewallet.notification.domain.event.AlertTriggeredDomainEvent;
import com.wisewallet.notification.domain.event.NotificationDispatchEvent;
import com.wisewallet.notification.domain.model.*;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import com.wisewallet.notification.domain.repository.ProcessedEventRepositoryPort;
import com.wisewallet.notification.domain.service.NotificationContentResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCommandService {

    private final NotificationRepositoryPort notificationRepository;
    private final ProcessedEventRepositoryPort processedEventRepository;
    private final PreferenceQueryService preferenceQueryService;
    private final IdempotencyChecker idempotencyChecker;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationContentResolver contentResolver;

    /**
     * Core entry point. Called by all Kafka consumers.
     * Idempotent: skips if eventId already processed.
     */
    @Transactional
    public void processEvent(UUID eventId,
                             EventType eventType,
                             UUID userId,
                             Map<String, Object> eventData) {
        if (idempotencyChecker.alreadyProcessed(eventId)) {
            log.debug("Skipping already-processed event: {}", eventId);
            return;
        }

        // resolve content
        NotificationContentResolver.NotificationContent content = contentResolver.resolve(eventType, eventData);

        // resolve enabled channels
        List<NotificationChannel> channels = preferenceQueryService.getEnabledChannels(userId, eventType);

        // persist processed_event + notifications in same transaction
        processedEventRepository.save(ProcessedEvent.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .eventType(eventType.name())
                .build());

        List<UUID> notificationIds = new ArrayList<>();
        for (NotificationChannel channel : channels) {
            NotificationStatus status = channel == NotificationChannel.IN_APP
                    ? NotificationStatus.UNREAD
                    : NotificationStatus.PENDING;

            Notification notification = notificationRepository.save(Notification.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .eventId(eventId)
                    .eventType(eventType)
                    .channel(channel)
                    .title(content.title())
                    .body(content.body())
                    .metadata(eventData)
                    .status(status)
                    .build());

            notificationIds.add(notification.getId());
        }

        // only BALANCE_LOW triggers an alert outbox event
        if (eventType == EventType.BALANCE_LOW) {
            publishAlertEvent(eventId, userId, eventData);
        }

        // dispatch event triggers SNS publishing AFTER commit
        if (!notificationIds.isEmpty()) {
            eventPublisher.publishEvent(new NotificationDispatchEvent(notificationIds));
        }
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new com.wisewallet.notification.domain.exception.NotificationNotFoundException(notificationId));
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    private void publishAlertEvent(UUID eventId, UUID userId, Map<String, Object> eventData) {
        UUID accountId = getUuid(eventData, "accountId");
        BigDecimal currentBalance = getBigDecimal(eventData, "currentBalance");
        BigDecimal threshold = getBigDecimal(eventData, "threshold");
        String currency = getOrDefault(eventData, "currency");

        eventPublisher.publishEvent(new AlertTriggeredDomainEvent(
                UUID.randomUUID(),
                userId,
                accountId,
                "BALANCE_LOW",
                currency,
                currentBalance,
                threshold,
                Instant.now()
        ));
    }

    private UUID getUuid(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v instanceof UUID u) return u;
        if (v instanceof String s) return UUID.fromString(s);
        return null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> data, String key) {
        Object v = data.get(key);
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (v instanceof String s) return new BigDecimal(s);
        return BigDecimal.ZERO;
    }

    private String getOrDefault(Map<String, Object> data, String key) {
        Object v = data.get(key);
        return v != null ? v.toString() : "";
    }
}
