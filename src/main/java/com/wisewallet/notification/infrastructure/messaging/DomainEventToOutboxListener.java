package com.wisewallet.notification.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.notification.domain.event.AlertTriggeredDomainEvent;
import com.wisewallet.notification.domain.model.OutboxEvent;
import com.wisewallet.notification.domain.repository.OutboxEventRepositoryPort;
import com.wisewallet.notification.infrastructure.messaging.event.AlertTriggeredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/**
 * Infrastructure listener — converts AlertTriggeredDomainEvent to an OutboxEvent
 * row BEFORE the outer transaction commits (guaranteeing at-least-once delivery).
 */
@Component
@RequiredArgsConstructor
public class DomainEventToOutboxListener {

    private final OutboxEventRepositoryPort outboxEventRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleAlertTriggered(AlertTriggeredDomainEvent domainEvent) {
        AlertTriggeredEvent payload = new AlertTriggeredEvent(
                domainEvent.eventId(),
                "ALERT_TRIGGERED",
                Instant.now(),
                domainEvent.userId(),
                domainEvent.accountId(),
                domainEvent.alertType(),
                domainEvent.currency(),
                domainEvent.currentBalance(),
                domainEvent.threshold()
        );

        outboxEventRepository.save(OutboxEvent.builder()
                .id(java.util.UUID.randomUUID())
                .aggregateType("notification")
                .aggregateId(domainEvent.userId())
                .eventType("alert.triggered")
                .payload(serialize(payload))
                .status("PENDING")
                .build());
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event payload", e);
        }
    }
}
