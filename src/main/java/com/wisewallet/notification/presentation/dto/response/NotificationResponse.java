package com.wisewallet.notification.presentation.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        String eventType,
        String channel,
        String title,
        String body,
        String status,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant readAt
) {}
