package com.wisewallet.notification.application.query;

import com.wisewallet.notification.domain.model.EventType;
import com.wisewallet.notification.domain.model.NotificationChannel;
import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.domain.repository.NotificationPreferenceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferenceQueryService {

    private final NotificationPreferenceRepositoryPort preferenceRepository;

    @Transactional(readOnly = true)
    public List<NotificationPreference> getPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId);
    }

    /**
     * Returns the enabled channels for a user / event type combination.
     * If no preference rows exist, all channels are enabled by default.
     */
    @Transactional(readOnly = true)
    public List<NotificationChannel> getEnabledChannels(UUID userId, EventType eventType) {
        List<NotificationPreference> prefs = preferenceRepository.findByUserIdAndEventType(userId, eventType);
        if (prefs.isEmpty()) {
            return Arrays.asList(NotificationChannel.values());
        }
        return prefs.stream()
                .filter(NotificationPreference::isEnabled)
                .map(NotificationPreference::getChannel)
                .toList();
    }
}
