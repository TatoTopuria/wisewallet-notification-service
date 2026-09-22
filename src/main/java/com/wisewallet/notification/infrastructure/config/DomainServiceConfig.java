package com.wisewallet.notification.infrastructure.config;

import com.wisewallet.notification.domain.service.NotificationContentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares domain services as Spring beans in the infrastructure configuration layer,
 * maintaining clean architecture principles with pure domain services.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public NotificationContentResolver notificationContentResolver() {
        return new NotificationContentResolver();
    }
}
