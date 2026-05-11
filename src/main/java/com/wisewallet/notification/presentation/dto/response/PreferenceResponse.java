package com.wisewallet.notification.presentation.dto.response;

import java.util.UUID;

public record PreferenceResponse(
        UUID id,
        String eventType,
        String channel,
        boolean enabled
) {}
