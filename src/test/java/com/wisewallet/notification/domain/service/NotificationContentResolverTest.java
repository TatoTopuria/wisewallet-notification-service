package com.wisewallet.notification.domain.service;

import com.wisewallet.notification.domain.model.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationContentResolverTest {

    private final NotificationContentResolver resolver = new NotificationContentResolver();

    @Test
    void resolve_accountCreated_returnsCorrectContent() {
        Map<String, Object> data = Map.of("accountType", "CHECKING");
        var content = resolver.resolve(EventType.ACCOUNT_CREATED, data);

        assertThat(content.title()).isEqualTo("Account Created");
        assertThat(content.body()).contains("CHECKING");
    }

    @Test
    void resolve_balanceLow_returnsCorrectContent() {
        Map<String, Object> data = Map.of(
                "currentBalance", "45.00",
                "currency", "USD",
                "threshold", "100.00"
        );
        var content = resolver.resolve(EventType.BALANCE_LOW, data);

        assertThat(content.title()).isEqualTo("Low Balance Alert");
        assertThat(content.body()).contains("45.00").contains("USD").contains("100.00");
    }

    @Test
    void resolve_txnCreated_returnsCorrectContent() {
        Map<String, Object> data = Map.of("type", "DEBIT", "amount", "50.00", "currency", "EUR");
        var content = resolver.resolve(EventType.TXN_CREATED, data);

        assertThat(content.title()).isEqualTo("Transaction Recorded");
        assertThat(content.body()).contains("DEBIT").contains("50.00").contains("EUR");
    }

    @Test
    void resolve_txnCategorized_returnsCorrectContent() {
        Map<String, Object> data = Map.of("category", "FOOD");
        var content = resolver.resolve(EventType.TXN_CATEGORIZED, data);

        assertThat(content.title()).isEqualTo("Transaction Categorized");
        assertThat(content.body()).contains("FOOD");
    }

    @Test
    void resolve_alertTriggered_returnsCorrectContent() {
        var content = resolver.resolve(EventType.ALERT_TRIGGERED, Map.of());

        assertThat(content.title()).isEqualTo("Financial Alert");
        assertThat(content.body()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void resolve_allEventTypes_neverReturnsNull(EventType eventType) {
        var content = resolver.resolve(eventType, Map.of());

        assertThat(content).isNotNull();
        assertThat(content.title()).isNotBlank();
        assertThat(content.body()).isNotNull();
    }

    @Test
    void resolve_missingKeys_usesEmptyDefault() {
        var content = resolver.resolve(EventType.ACCOUNT_CREATED, Map.of());

        assertThat(content.title()).isEqualTo("Account Created");
        assertThat(content.body()).isNotNull();
    }
}
