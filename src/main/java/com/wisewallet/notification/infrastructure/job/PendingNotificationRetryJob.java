package com.wisewallet.notification.infrastructure.job;

import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.model.NotificationStatus;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import com.wisewallet.notification.infrastructure.sns.SnsNotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scans for PENDING email notifications that were not dispatched via SNS
 * (e.g. SNS was unavailable) and re-publishes them.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingNotificationRetryJob {

    private final NotificationRepositoryPort notificationRepository;
    private final SnsNotificationDispatcher snsNotificationDispatcher;

    @Value("${wisewallet.notification.retry.max-sns-retries:5}")
    private int maxSnsRetries;

    @Scheduled(fixedDelayString = "${wisewallet.notification.retry.pending-check-interval-ms:30000}")
    @Transactional
    public void retryPendingNotifications() {
        Instant cutoff = Instant.now().minus(60, ChronoUnit.SECONDS);
        List<Notification> pending = notificationRepository.findPendingEmailOlderThan(cutoff);

        for (Notification notification : pending) {
            if (notification.getRetryCount() >= maxSnsRetries) {
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
                log.warn("Marking notification FAILED after {} retries: id={}", maxSnsRetries, notification.getId());
                continue;
            }
            try {
                snsNotificationDispatcher.publish(notification);
                notification.setRetryCount(notification.getRetryCount() + 1);
                notificationRepository.save(notification);
                log.debug("Re-published pending notification id={} retry={}", notification.getId(), notification.getRetryCount());
            } catch (Exception e) {
                log.error("Retry failed for notification id={}: {}", notification.getId(), e.getMessage());
            }
        }
    }
}
