package com.wisewallet.notification.application.port.out;

import java.util.UUID;

public record UserInfo(UUID userId, String email, String firstName, String lastName) {}
