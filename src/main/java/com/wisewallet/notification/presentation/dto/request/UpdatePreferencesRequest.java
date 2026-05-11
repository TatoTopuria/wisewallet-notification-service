package com.wisewallet.notification.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePreferencesRequest(
        @NotEmpty @Valid List<PreferenceEntry> preferences
) {
    public record PreferenceEntry(
            @NotNull String eventType,
            @NotNull String channel,
            boolean enabled
    ) {}
}
