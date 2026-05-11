package com.wisewallet.notification.infrastructure.client.config;

import feign.RequestInterceptor;
import feign.Request;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class AccountServiceFeignConfig {

    @Value("${wisewallet.internal.api-key:dev-internal-key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor internalKeyInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-Key", internalApiKey);
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                requestTemplate.header("X-Correlation-ID", correlationId);
            }
        };
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(2, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }
}
