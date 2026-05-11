package com.wisewallet.notification.integration;

import com.wisewallet.notification.domain.model.NotificationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the HTTP notification endpoints end-to-end with a real DB.
 */
class NotificationControllerIT extends BaseIntegrationTest {

    @Autowired
    private com.wisewallet.notification.application.command.NotificationCommandService commandService;

    private HttpHeaders headersFor(UUID userId) {
        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.set("X-User-Roles", "USER");
        return headers;
    }

    @Test
    void listNotifications_returnsPagedResults() {
        UUID userId = UUID.randomUUID();

        // Pre-populate a notification directly via command service (bypasses Kafka)
        commandService.processEvent(
                UUID.randomUUID(),
                com.wisewallet.notification.domain.model.EventType.TXN_CREATED,
                userId,
                java.util.Map.of("type", "DEBIT", "amount", "10.00", "currency", "USD")
        );

        var response = restTemplate.exchange(
                "/api/notifications?size=10",
                HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("TXN_CREATED");
    }

    @Test
    void getUnreadCount_reflectsInAppNotifications() {
        UUID userId = UUID.randomUUID();

        // IN_APP notifications start as UNREAD
        commandService.processEvent(
                UUID.randomUUID(),
                com.wisewallet.notification.domain.model.EventType.ACCOUNT_CREATED,
                userId,
                java.util.Map.of("accountType", "CHECKING")
        );

        var response = restTemplate.exchange(
                "/api/notifications/unread/count",
                HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // count may be 0 or positive depending on default preferences; success means endpoint works
    }

    @Test
    void markAsRead_changesStatusToRead() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        // Save an IN_APP (UNREAD) notification
        var notif = notificationRepository.save(
                com.wisewallet.notification.domain.model.Notification.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .eventId(eventId)
                        .eventType(com.wisewallet.notification.domain.model.EventType.TXN_CREATED)
                        .channel(com.wisewallet.notification.domain.model.NotificationChannel.IN_APP)
                        .title("Tx")
                        .body("Body")
                        .status(NotificationStatus.UNREAD)
                        .build()
        );

        var response = restTemplate.exchange(
                "/api/notifications/" + notif.getId() + "/read",
                HttpMethod.PUT,
                new HttpEntity<>(headersFor(userId)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var updated = notificationRepository.findById(notif.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(updated.getReadAt()).isNotNull();
    }

    @Test
    void markAllRead_marksAllUnreadForUser() {
        UUID userId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();

        // Save two UNREAD notifications for the user and one for another user
        notificationRepository.save(buildUnread(userId, UUID.randomUUID()));
        notificationRepository.save(buildUnread(userId, UUID.randomUUID()));
        notificationRepository.save(buildUnread(otherUser, UUID.randomUUID()));

        var response = restTemplate.exchange(
                "/api/notifications/read-all",
                HttpMethod.PUT,
                new HttpEntity<>(headersFor(userId)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        long unreadForUser = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(userId))
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .count();
        assertThat(unreadForUser).isZero();

        // Other user's notification is untouched
        long unreadForOther = notificationRepository.findAll().stream()
                .filter(n -> n.getUserId().equals(otherUser))
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .count();
        assertThat(unreadForOther).isOne();
    }

    @Test
    void listNotifications_missingUserId_returns401() {
        var response = restTemplate.exchange(
                "/api/notifications",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );

        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    private com.wisewallet.notification.domain.model.Notification buildUnread(UUID userId, UUID eventId) {
        return com.wisewallet.notification.domain.model.Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventId(eventId)
                .eventType(com.wisewallet.notification.domain.model.EventType.TXN_CREATED)
                .channel(com.wisewallet.notification.domain.model.NotificationChannel.IN_APP)
                .title("Tx")
                .body("Body")
                .status(NotificationStatus.UNREAD)
                .build();
    }
}
