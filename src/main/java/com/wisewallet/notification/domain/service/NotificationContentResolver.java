package com.wisewallet.notification.domain.service;

import com.wisewallet.notification.domain.model.EventType;

import java.util.Map;

/**
 * Pure domain service — no Spring annotations.
 * Builds human-readable notification title and body per event type.
 */
public class NotificationContentResolver {

    public record NotificationContent(String title, String body) {}

    public NotificationContent resolve(EventType eventType, Map<String, Object> eventData) {
        return switch (eventType) {
            case ACCOUNT_CREATED -> new NotificationContent(
                    "Account Created",
                    "Your new %s account has been created.".formatted(
                            getOrDefault(eventData, "accountType", ""))
            );
            case BALANCE_LOW -> new NotificationContent(
                    "Low Balance Alert",
                    "Account balance %s %s is below threshold %s.".formatted(
                            getOrDefault(eventData, "currentBalance", ""),
                            getOrDefault(eventData, "currency", ""),
                            getOrDefault(eventData, "threshold", ""))
            );
            case TXN_CREATED -> new NotificationContent(
                    "Transaction Recorded",
                    "%s: %s %s".formatted(
                            getOrDefault(eventData, "type", ""),
                            getOrDefault(eventData, "amount", ""),
                            getOrDefault(eventData, "currency", ""))
            );
            case TXN_CATEGORIZED -> new NotificationContent(
                    "Transaction Categorized",
                    "Transaction categorized as %s".formatted(
                            getOrDefault(eventData, "category", ""))
            );
            case ALERT_TRIGGERED -> new NotificationContent(
                    "Financial Alert",
                    "A financial alert has been triggered for your account."
            );
        };
    }

    private String getOrDefault(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
