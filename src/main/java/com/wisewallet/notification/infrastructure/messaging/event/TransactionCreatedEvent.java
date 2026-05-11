package com.wisewallet.notification.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        UUID transactionId,
        UUID transferId,
        UUID userId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String type,
        String status,
        Instant createdAt
) {}
