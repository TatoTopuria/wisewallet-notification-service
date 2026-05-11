package com.wisewallet.notification.infrastructure.sns;

import com.wisewallet.notification.domain.event.NotificationDispatchEvent;
import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Map;
import java.util.UUID;

/**
 * After the notification transaction commits, publishes each notification
 * to SNS with channel + eventType message attributes for filter routing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnsDispatchListener {

    private final NotificationRepositoryPort notificationRepository;
    private final SnsNotificationDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDispatch(NotificationDispatchEvent event) {
        for (UUID notificationId : event.notificationIds()) {
            notificationRepository.findById(notificationId).ifPresent(notification -> {
                if (notification.getChannel() != NotificationChannel.IN_APP) {
                    try {
                        dispatcher.publish(notification);
                    } catch (Exception e) {
                        log.error("SNS dispatch failed for notificationId={}: {}",
                                notificationId, e.getMessage(), e);
                    }
                }
            });
        }
    }
}
