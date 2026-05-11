package com.wisewallet.notification.infrastructure.client;

import com.wisewallet.notification.application.port.out.AccountServicePort;
import com.wisewallet.notification.application.port.out.UserInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountServiceAdapter implements AccountServicePort {

    private final AccountServiceFeignClient feignClient;

    @Override
    @Cacheable(value = "userInfo", key = "#userId")
    @CircuitBreaker(name = "accountService", fallbackMethod = "getUserInfoFallback")
    public UserInfo getUserInfo(UUID userId) {
        AccountServiceFeignClient.UserInfoResponse response = feignClient.getUserInfo(userId);
        return new UserInfo(response.userId(), response.email(), response.firstName(), response.lastName());
    }

    public UserInfo getUserInfoFallback(UUID userId, Exception ex) {
        log.warn("Circuit breaker open for account-service, userId={}: {}", userId, ex.getMessage());
        return new UserInfo(userId, null, null, null);
    }
}
