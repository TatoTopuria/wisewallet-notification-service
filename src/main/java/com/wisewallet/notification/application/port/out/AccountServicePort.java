package com.wisewallet.notification.application.port.out;

import java.util.UUID;

/**
 * Output port for fetching user contact information from account-service.
 */
public interface AccountServicePort {
    UserInfo getUserInfo(UUID userId);
}
