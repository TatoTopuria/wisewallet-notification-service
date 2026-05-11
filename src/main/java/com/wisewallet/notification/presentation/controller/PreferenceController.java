package com.wisewallet.notification.presentation.controller;

import com.wisewallet.notification.application.command.PreferenceCommandService;
import com.wisewallet.notification.application.query.PreferenceQueryService;
import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.presentation.dto.request.UpdatePreferencesRequest;
import com.wisewallet.notification.presentation.dto.response.PreferenceResponse;
import com.wisewallet.notification.presentation.mapper.PreferenceMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceQueryService preferenceQueryService;
    private final PreferenceCommandService preferenceCommandService;
    private final PreferenceMapper preferenceMapper;

    @GetMapping
    public List<PreferenceResponse> getPreferences(@AuthenticationPrincipal String userId) {
        return preferenceQueryService.getPreferences(UUID.fromString(userId)).stream()
                .map(preferenceMapper::toResponse)
                .toList();
    }

    @PutMapping
    public List<PreferenceResponse> updatePreferences(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        List<PreferenceCommandService.PreferenceEntry> entries = request.preferences().stream()
                .map(e -> new PreferenceCommandService.PreferenceEntry(e.eventType(), e.channel(), e.enabled()))
                .toList();
        List<NotificationPreference> updated = preferenceCommandService.upsertPreferences(
                UUID.fromString(userId), entries);
        return updated.stream().map(preferenceMapper::toResponse).toList();
    }
}
