package com.wisewallet.notification.domain.repository;

import com.wisewallet.notification.domain.model.ProcessedEvent;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventRepositoryPort {
    ProcessedEvent save(ProcessedEvent event);
    boolean existsByEventId(UUID eventId);
    int deleteByProcessedAtBefore(Instant cutoff);
}
