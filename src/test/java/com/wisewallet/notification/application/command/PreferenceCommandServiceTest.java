package com.wisewallet.notification.application.command;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.domain.repository.NotificationPreferenceRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreferenceCommandServiceTest {

    @Mock
    NotificationPreferenceRepositoryPort preferenceRepository;

    @InjectMocks
    PreferenceCommandService service;

    @Test
    void upsertPreferences_createsNewPreferenceWhenNotFound() {
        UUID userId = UUID.randomUUID();
        when(preferenceRepository.findByUserIdAndEventTypeAndChannel(userId, EventType.ACCOUNT_CREATED, NotificationChannel.EMAIL))
                .thenReturn(Optional.empty());
        when(preferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var entry = new PreferenceCommandService.PreferenceEntry("ACCOUNT_CREATED", "EMAIL", true);
        List<NotificationPreference> result = service.upsertPreferences(userId, List.of(entry));

        assertThat(result).hasSize(1);
        ArgumentCaptor<NotificationPreference> captor = ArgumentCaptor.forClass(NotificationPreference.class);
        verify(preferenceRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.ACCOUNT_CREATED);
        assertThat(captor.getValue().getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(captor.getValue().isEnabled()).isTrue();
    }

    @Test
    void upsertPreferences_updatesExistingPreference() {
        UUID userId = UUID.randomUUID();
        NotificationPreference existing = NotificationPreference.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .eventType(EventType.BALANCE_LOW)
                .channel(NotificationChannel.EMAIL)
                .enabled(true)
                .build();

        when(preferenceRepository.findByUserIdAndEventTypeAndChannel(userId, EventType.BALANCE_LOW, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(existing));
        when(preferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var entry = new PreferenceCommandService.PreferenceEntry("BALANCE_LOW", "EMAIL", false);
        service.upsertPreferences(userId, List.of(entry));

        assertThat(existing.isEnabled()).isFalse();
        verify(preferenceRepository).save(existing);
    }

    @Test
    void upsertPreferences_handlesMultipleEntries() {
        UUID userId = UUID.randomUUID();
        when(preferenceRepository.findByUserIdAndEventTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(preferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<PreferenceCommandService.PreferenceEntry> entries = List.of(
                new PreferenceCommandService.PreferenceEntry("ACCOUNT_CREATED", "EMAIL", true),
                new PreferenceCommandService.PreferenceEntry("BALANCE_LOW", "IN_APP", false)
        );

        List<NotificationPreference> result = service.upsertPreferences(userId, entries);

        assertThat(result).hasSize(2);
        verify(preferenceRepository, times(2)).save(any());
    }

    @Test
    void upsertPreferences_throwsOnInvalidEventType() {
        UUID userId = UUID.randomUUID();
        var entry = new PreferenceCommandService.PreferenceEntry("INVALID_EVENT", "EMAIL", true);

        assertThatThrownBy(() -> service.upsertPreferences(userId, List.of(entry)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void upsertPreferences_throwsOnInvalidChannel() {
        UUID userId = UUID.randomUUID();
        when(preferenceRepository.findByUserIdAndEventTypeAndChannel(any(), any(), any()))
                .thenReturn(Optional.empty());
        var entry = new PreferenceCommandService.PreferenceEntry("ACCOUNT_CREATED", "INVALID_CHANNEL", true);

        assertThatThrownBy(() -> service.upsertPreferences(userId, List.of(entry)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
