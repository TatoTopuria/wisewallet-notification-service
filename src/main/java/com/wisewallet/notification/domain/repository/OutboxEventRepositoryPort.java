package com.wisewallet.notification.domain.repository;

import com.wisewallet.notification.domain.model.OutboxEvent;

import java.util.List;

public interface OutboxEventRepositoryPort {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findPendingEvents(int limit);
    int markPublished(OutboxEvent event);
}
