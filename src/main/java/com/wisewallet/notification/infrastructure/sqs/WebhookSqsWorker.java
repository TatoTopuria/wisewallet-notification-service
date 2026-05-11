package com.wisewallet.notification.infrastructure.sqs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.model.NotificationStatus;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * V1 webhook worker stub: accepts SNS->SQS messages, logs receipt, and marks
 * the notification as delivered without performing an outbound webhook call.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookSqsWorker {

    private final NotificationRepositoryPort notificationRepository;
    private final ObjectMapper objectMapper;

    @SqsListener("wisewallet-webhook")
    @Transactional
    public void processWebhookMessage(String messageBody) {
        Map<String, String> msg = deserialize(messageBody);
        UUID notificationId = UUID.fromString(msg.get("notificationId"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() != NotificationStatus.PENDING) {
            log.debug("Skipping webhook delivery for non-PENDING notification: id={} status={}",
                    notificationId, notification.getStatus());
            return;
        }

        log.info("Webhook stub accepted message: notificationId={} eventType={} userId={}",
                notificationId, msg.get("eventType"), msg.get("userId"));

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        notificationRepository.save(notification);
    }

    private Map<String, String> deserialize(String body) {
        try {
            Map<String, Object> outer = objectMapper.readValue(body, new TypeReference<>() {});
            if (outer.containsKey("Message")) {
                return objectMapper.readValue((String) outer.get("Message"), new TypeReference<>() {});
            }
            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize SQS message: " + body, e);
        }
    }
}
