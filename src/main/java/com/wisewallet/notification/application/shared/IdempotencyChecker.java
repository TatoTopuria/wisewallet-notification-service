package com.wisewallet.notification.application.shared;

import com.wisewallet.notification.domain.repository.ProcessedEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Wraps the ProcessedEventRepositoryPort with a simple boolean API for
 * Kafka consumer idempotency checks.
 */
@Component
@RequiredArgsConstructor
public class IdempotencyChecker {

    private final ProcessedEventRepositoryPort processedEventRepository;

    public boolean alreadyProcessed(UUID eventId) {
        return processedEventRepository.existsByEventId(eventId);
    }
}
