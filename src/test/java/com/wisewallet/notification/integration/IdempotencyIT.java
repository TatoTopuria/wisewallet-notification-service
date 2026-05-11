package com.wisewallet.notification.integration;

import com.wisewallet.notification.application.command.NotificationCommandService;
import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that replaying a Kafka event with the same eventId is idempotent.
 */
class IdempotencyIT extends BaseIntegrationTest {

    @Autowired
    NotificationCommandService commandService;

    @Test
    void processEvent_calledTwiceWithSameEventId_createsOnlyOneProcessedEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Map<String, Object> data = Map.of("accountType", "CHECKING");

        commandService.processEvent(eventId, EventType.ACCOUNT_CREATED, userId, data);
        commandService.processEvent(eventId, EventType.ACCOUNT_CREATED, userId, data);

        long processedCount = processedEventRepository.findAll().stream()
                .filter(e -> e.getEventId().equals(eventId))
                .count();
        assertThat(processedCount).isOne();
    }

    @Test
    void processEvent_differentEventIds_createsSeparateRecords() {
        UUID userId = UUID.randomUUID();
        Map<String, Object> data = Map.of("accountType", "CHECKING");

        commandService.processEvent(UUID.randomUUID(), EventType.ACCOUNT_CREATED, userId, data);
        commandService.processEvent(UUID.randomUUID(), EventType.ACCOUNT_CREATED, userId, data);

        long count = processedEventRepository.findAll().stream()
                .filter(e -> e.getEventType().equals("ACCOUNT_CREATED"))
                .count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    void processEvent_skipsWhenIdempotencyKeyExists_doesNotCreateDuplicateNotifications() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Enable at least one channel via preference
        preferenceRepository.save(NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventType(EventType.TXN_CREATED)
                .channel(NotificationChannel.IN_APP)
                .enabled(true)
                .build());

        Map<String, Object> data = Map.of("type", "DEBIT", "amount", "10", "currency", "USD");

        commandService.processEvent(eventId, EventType.TXN_CREATED, userId, data);
        commandService.processEvent(eventId, EventType.TXN_CREATED, userId, data); // replay

        long notifCount = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(userId) && n.getEventId().equals(eventId))
                .count();
        assertThat(notifCount).isOne();
    }
}
