package com.wisewallet.notification.domain.repository;

import com.wisewallet.notification.domain.model.ProcessedEvent;

import java.time.Instant;

public interface ProcessedEventRepositoryPort {
    ProcessedEvent save(ProcessedEvent event);
    int deleteByProcessedAtBefore(Instant cutoff);
}
