package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.ProcessedEvent;
import com.wisewallet.notification.domain.repository.ProcessedEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepositoryPort {

    private final ProcessedEventJpaRepository jpaRepository;

    @Override
    public ProcessedEvent save(ProcessedEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return jpaRepository.existsByEventId(eventId);
    }

    @Override
    public int deleteByProcessedAtBefore(Instant cutoff) {
        return jpaRepository.deleteByProcessedAtBefore(cutoff);
    }
}
