package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.OutboxEvent;
import com.wisewallet.notification.domain.repository.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryAdapter implements OutboxEventRepositoryPort {

    private final OutboxEventJpaRepository jpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return jpaRepository.save(event);
    }

    @Override
    public List<OutboxEvent> findPendingEvents(int limit) {
        return jpaRepository.findPendingEvents(limit);
    }

    @Override
    @Transactional
    public int markPublished(OutboxEvent event) {
        event.setStatus("PUBLISHED");
        event.setPublishedAt(Instant.now());
        jpaRepository.save(event);
        return 1;
    }
}
