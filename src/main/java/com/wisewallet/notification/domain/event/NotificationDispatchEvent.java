package com.wisewallet.notification.domain.event;

import java.util.List;
import java.util.UUID;

/**
 * Domain event published after notification rows are committed, triggering
 * SNS dispatch via @TransactionalEventListener(AFTER_COMMIT).
 */
public record NotificationDispatchEvent(
        List<UUID> notificationIds
) implements DomainEvent {}
