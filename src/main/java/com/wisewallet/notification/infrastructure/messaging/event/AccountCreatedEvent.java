package com.wisewallet.notification.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID userId,
        UUID accountId,
        String accountType,
        String currency,
        String nickname
) {}
