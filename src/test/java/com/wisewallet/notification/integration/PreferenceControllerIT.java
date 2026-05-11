package com.wisewallet.notification.integration;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.presentation.dto.request.UpdatePreferencesRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for preference endpoints.
 */
class PreferenceControllerIT extends BaseIntegrationTest {

    private HttpHeaders headersFor(UUID userId) {
        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.set("X-User-Roles", "USER");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void getPreferences_noPreferences_returnsEmptyList() {
        UUID userId = UUID.randomUUID();

        var response = restTemplate.exchange(
                "/api/notifications/preferences",
                HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("[]");
    }

    @Test
    void getPreferences_existingPreferences_returnsList() {
        UUID userId = UUID.randomUUID();
        preferenceRepository.save(NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventType(EventType.ACCOUNT_CREATED)
                .channel(NotificationChannel.EMAIL)
                .enabled(true)
                .build());

        var response = restTemplate.exchange(
                "/api/notifications/preferences",
                HttpMethod.GET,
                new HttpEntity<>(headersFor(userId)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("ACCOUNT_CREATED").contains("EMAIL");
    }

    @Test
    void updatePreferences_createsNewPreferences() {
        UUID userId = UUID.randomUUID();
        var request = new UpdatePreferencesRequest(List.of(
                new UpdatePreferencesRequest.PreferenceEntry("BALANCE_LOW", "EMAIL", true),
                new UpdatePreferencesRequest.PreferenceEntry("TXN_CREATED", "IN_APP", false)
        ));

        var response = restTemplate.exchange(
                "/api/notifications/preferences",
                HttpMethod.PUT,
                new HttpEntity<>(request, headersFor(userId)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("BALANCE_LOW").contains("TXN_CREATED");

        long count = preferenceRepository.findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .count();
        assertThat(count).isEqualTo(2);
    }

    @Test
    void updatePreferences_updatesExistingPreference() {
        UUID userId = UUID.randomUUID();
        preferenceRepository.save(NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventType(EventType.BALANCE_LOW)
                .channel(NotificationChannel.EMAIL)
                .enabled(true)
                .build());

        var request = new UpdatePreferencesRequest(List.of(
                new UpdatePreferencesRequest.PreferenceEntry("BALANCE_LOW", "EMAIL", false)
        ));

        var response = restTemplate.exchange(
                "/api/notifications/preferences",
                HttpMethod.PUT,
                new HttpEntity<>(request, headersFor(userId)),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        var updated = preferenceRepository.findByUserId(userId).stream()
                .filter(p -> p.getEventType() == EventType.BALANCE_LOW && p.getChannel() == NotificationChannel.EMAIL)
                .findFirst()
                .orElseThrow();
        assertThat(updated.isEnabled()).isFalse();
    }

    @Test
    void updatePreferences_invalidEventType_returns400() {
        UUID userId = UUID.randomUUID();
        String body = """
                {"preferences":[{"eventType":"INVALID","channel":"EMAIL","enabled":true}]}
                """;

        var headers = headersFor(userId);
        var response = restTemplate.exchange(
                "/api/notifications/preferences",
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                String.class
        );

        assertThat(response.getStatusCode().value()).isIn(400, 500);
    }
}
