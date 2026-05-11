package com.wisewallet.notification.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a BALANCE_LOW Kafka event is processed, to be
 * forwarded (via outbox) to the alert.triggered Kafka topic for advisor-service.
 */
public record AlertTriggeredDomainEvent(
        UUID eventId,
        UUID userId,
        UUID accountId,
        String alertType,
        String currency,
        BigDecimal currentBalance,
        BigDecimal threshold,
        Instant occurredAt
) implements DomainEvent {}
