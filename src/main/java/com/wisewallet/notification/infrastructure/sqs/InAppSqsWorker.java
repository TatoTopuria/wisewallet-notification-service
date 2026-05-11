package com.wisewallet.notification.infrastructure.sqs;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * In-app push SQS worker. The notification is already stored as UNREAD by
 * the Kafka consumer, so this worker is a no-op placeholder for future FCM support.
 */
@Component
@Slf4j
public class InAppSqsWorker {

    @SqsListener("wisewallet-push")
    public void processInAppMessage(String messageBody) {
        log.debug("In-app SQS message received (no-op): {}", messageBody);
        // Notification already stored as UNREAD in DB during Kafka consumer processing.
        // This queue exists for future FCM/APNs push notification dispatch.
    }
}
