package com.wisewallet.notification.infrastructure.messaging;

import com.wisewallet.notification.domain.model.OutboxEvent;
import com.wisewallet.notification.domain.repository.OutboxEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Polls the outbox table every 2 seconds and publishes PENDING events to Kafka.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OutboxEventRepositoryPort outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${wisewallet.notification.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${wisewallet.notification.outbox.poll-fixed-delay-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findPendingEvents(batchSize);
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId().toString(), event.getPayload());
                outboxEventRepository.markPublished(event);
                log.debug("Published outbox event: type={} aggregateId={}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
}
