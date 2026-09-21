package com.wisewallet.notification.application.command;

import com.wisewallet.notification.domain.event.AlertTriggeredDomainEvent;
import com.wisewallet.notification.domain.event.NotificationDispatchEvent;
import com.wisewallet.notification.domain.model.*;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import com.wisewallet.notification.domain.repository.ProcessedEventRepositoryPort;
import com.wisewallet.notification.domain.service.NotificationContentResolver;
import com.wisewallet.notification.application.query.PreferenceQueryService;
import com.wisewallet.notification.domain.exception.NotificationNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationCommandServiceTest {

    @Mock NotificationRepositoryPort notificationRepository;
    @Mock ProcessedEventRepositoryPort processedEventRepository;
    @Mock PreferenceQueryService preferenceQueryService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock NotificationContentResolver contentResolver;

    @InjectMocks
    NotificationCommandService service;

    @Test
    void processEvent_skipsWhenAlreadyProcessed() {
        UUID eventId = UUID.randomUUID();
        when(processedEventRepository.save(any(ProcessedEvent.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate event"));

        service.processEvent(eventId, EventType.ACCOUNT_CREATED, UUID.randomUUID(), Map.of());

        verifyNoInteractions(notificationRepository, eventPublisher);
    }

    @Test
    void processEvent_savesNotificationPerEnabledChannel() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Map<String, Object> eventData = Map.of("accountType", "CHECKING");

        when(contentResolver.resolve(EventType.ACCOUNT_CREATED, eventData))
                .thenReturn(new NotificationContentResolver.NotificationContent("Account Created", "Your account was created."));
        when(preferenceQueryService.getEnabledChannels(userId, EventType.ACCOUNT_CREATED))
                .thenReturn(List.of(NotificationChannel.EMAIL, NotificationChannel.IN_APP));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(eventId, EventType.ACCOUNT_CREATED, userId, eventData);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(processedEventRepository).save(any(ProcessedEvent.class));

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(NotificationDispatchEvent.class);
        assertThat(((NotificationDispatchEvent) captor.getValue()).notificationIds()).hasSize(2);
    }

    @Test
    void processEvent_inAppChannelGetsUnreadStatus() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(contentResolver.resolve(any(), any()))
                .thenReturn(new NotificationContentResolver.NotificationContent("title", "body"));
        when(preferenceQueryService.getEnabledChannels(userId, EventType.TXN_CREATED))
                .thenReturn(List.of(NotificationChannel.IN_APP));

        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(notifCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(eventId, EventType.TXN_CREATED, userId, Map.of());

        assertThat(notifCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.UNREAD);
    }

    @Test
    void processEvent_emailChannelGetsPendingStatus() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(contentResolver.resolve(any(), any()))
                .thenReturn(new NotificationContentResolver.NotificationContent("title", "body"));
        when(preferenceQueryService.getEnabledChannels(userId, EventType.TXN_CREATED))
                .thenReturn(List.of(NotificationChannel.EMAIL));

        ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(notifCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(eventId, EventType.TXN_CREATED, userId, Map.of());

        assertThat(notifCaptor.getValue().getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void processEvent_balanceLow_publishesAlertDomainEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Map<String, Object> eventData = Map.of(
                "accountId", UUID.randomUUID().toString(),
                "currency", "USD",
                "currentBalance", "50.00",
                "threshold", "100.00"
        );

        when(contentResolver.resolve(any(), any()))
                .thenReturn(new NotificationContentResolver.NotificationContent("Low Balance", "Balance is low"));
        when(preferenceQueryService.getEnabledChannels(userId, EventType.BALANCE_LOW))
                .thenReturn(List.of(NotificationChannel.EMAIL));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(eventId, EventType.BALANCE_LOW, userId, eventData);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        List<Object> events = captor.getAllValues();
        assertThat(events).anyMatch(e -> e instanceof AlertTriggeredDomainEvent);
        assertThat(events).anyMatch(e -> e instanceof NotificationDispatchEvent);
    }

    @Test
    void processEvent_accountCreated_doesNotPublishAlertDomainEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(contentResolver.resolve(any(), any()))
                .thenReturn(new NotificationContentResolver.NotificationContent("title", "body"));
        when(preferenceQueryService.getEnabledChannels(any(), any()))
                .thenReturn(List.of(NotificationChannel.EMAIL));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(processedEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(eventId, EventType.ACCOUNT_CREATED, userId, Map.of());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isNotInstanceOf(AlertTriggeredDomainEvent.class);
    }

    @Test
    void processEvent_noEnabledChannels_doesNotPublishDispatchEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(contentResolver.resolve(any(), any()))
                .thenReturn(new NotificationContentResolver.NotificationContent("title", "body"));
        when(preferenceQueryService.getEnabledChannels(any(), any())).thenReturn(List.of());
        when(processedEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.processEvent(eventId, EventType.ACCOUNT_CREATED, userId, Map.of());

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void markAsRead_updatesStatusAndReadAt() {
        UUID notifId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notifId)
                .userId(userId)
                .status(NotificationStatus.UNREAD)
                .build();

        when(notificationRepository.findByIdAndUserId(notifId, userId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.markAsRead(notifId, userId);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    void markAsRead_throwsWhenNotificationNotFound() {
        UUID notifId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(notificationRepository.findByIdAndUserId(notifId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(notifId, userId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void markAllRead_delegatesToRepository() {
        UUID userId = UUID.randomUUID();
        service.markAllRead(userId);
        verify(notificationRepository).markAllReadByUserId(userId);
    }
}
