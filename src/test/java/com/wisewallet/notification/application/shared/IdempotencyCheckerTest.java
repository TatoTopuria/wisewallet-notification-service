package com.wisewallet.notification.application.shared;

import com.wisewallet.notification.domain.repository.ProcessedEventRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyCheckerTest {

    @Mock
    ProcessedEventRepositoryPort processedEventRepository;

    @InjectMocks
    IdempotencyChecker idempotencyChecker;

    @Test
    void alreadyProcessed_returnsTrueWhenEventExists() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsByEventId(eventId)).thenReturn(true);

        assertThat(idempotencyChecker.alreadyProcessed(eventId)).isTrue();
        verify(processedEventRepository).existsByEventId(eventId);
    }

    @Test
    void alreadyProcessed_returnsFalseWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.existsByEventId(eventId)).thenReturn(false);

        assertThat(idempotencyChecker.alreadyProcessed(eventId)).isFalse();
    }
}
