package com.wisewallet.notification.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCategorizedEvent(
        UUID eventId,
        UUID transactionId,
        UUID userId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String category,
        Instant categorizedAt
) {}
