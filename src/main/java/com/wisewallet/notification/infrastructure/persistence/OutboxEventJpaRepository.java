package com.wisewallet.notification.infrastructure.persistence;

import com.wisewallet.notification.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = "SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC LIMIT :limit")
    List<OutboxEvent> findPendingEvents(@Param("limit") int limit);
}
