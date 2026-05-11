package com.wisewallet.notification.infrastructure.messaging.consumer;

import com.wisewallet.notification.application.command.NotificationCommandService;
import com.wisewallet.notification.domain.model.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertEventConsumer {

    private final NotificationCommandService notificationCommandService;

    @KafkaListener(
            topics = "${wisewallet.notification.kafka.topics.alert-triggered:alert.triggered}",
            containerFactory = "accountEventListenerFactory"
    )
    public void onAlertTriggered(ConsumerRecord<String, Map<String, Object>> record) {
        propagateCorrelationId(record);
        Map<String, Object> payload = record.value();
        try {
            UUID eventId = toUuid(payload.get("eventId"));
            UUID userId = toUuid(payload.get("userId"));

            log.debug("Processing alert.triggered eventId={} userId={}", eventId, userId);
            // ALERT_TRIGGERED does NOT re-publish AlertTriggeredDomainEvent (loop prevention)
            notificationCommandService.processEvent(eventId, EventType.ALERT_TRIGGERED, userId, new HashMap<>(payload));
        } catch (Exception e) {
            log.error("Failed processing alert.triggered: {}", payload, e);
            throw e;
        }
    }

    private void propagateCorrelationId(ConsumerRecord<?, ?> record) {
        var header = record.headers().lastHeader("X-Correlation-ID");
        if (header != null) {
            MDC.put("correlationId", new String(header.value()));
        }
    }

    private UUID toUuid(Object value) {
        if (value instanceof String s) return UUID.fromString(s);
        if (value instanceof UUID u) return u;
        throw new IllegalArgumentException("Cannot convert to UUID: " + value);
    }
}
