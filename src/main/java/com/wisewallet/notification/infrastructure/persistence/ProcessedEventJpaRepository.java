package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEvent, UUID> {

    boolean existsByEventId(UUID eventId);

    @Modifying
    @Query("DELETE FROM ProcessedEvent e WHERE e.processedAt < :cutoff")
    int deleteByProcessedAtBefore(@Param("cutoff") Instant cutoff);
}
