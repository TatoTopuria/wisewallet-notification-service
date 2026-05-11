package com.wisewallet.notification.application.command;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.domain.repository.NotificationPreferenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferenceCommandService {

    private final NotificationPreferenceRepositoryPort preferenceRepository;

    /**
     * Bulk upsert: for each entry, update existing row or create a new one.
     */
    @Transactional
    public List<NotificationPreference> upsertPreferences(UUID userId,
                                                           List<PreferenceEntry> entries) {
        return entries.stream()
                .map(entry -> upsert(userId, entry))
                .toList();
    }

    private NotificationPreference upsert(UUID userId, PreferenceEntry entry) {
        EventType eventType = EventType.valueOf(entry.eventType());
        NotificationChannel channel = NotificationChannel.valueOf(entry.channel());

        return preferenceRepository.findByUserIdAndEventTypeAndChannel(userId, eventType, channel)
                .map(existing -> {
                    existing.setEnabled(entry.enabled());
                    return preferenceRepository.save(existing);
                })
                .orElseGet(() -> preferenceRepository.save(
                        NotificationPreference.builder()
                                .id(UUID.randomUUID())
                                .userId(userId)
                                .eventType(eventType)
                                .channel(channel)
                                .enabled(entry.enabled())
                                .build()
                ));
    }

    public record PreferenceEntry(String eventType, String channel, boolean enabled) {}
}
