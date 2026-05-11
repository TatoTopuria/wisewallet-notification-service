package com.wisewallet.notification.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AlertTriggeredEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID userId,
        UUID accountId,
        String alertType,
        String currency,
        BigDecimal currentBalance,
        BigDecimal threshold
) {}
