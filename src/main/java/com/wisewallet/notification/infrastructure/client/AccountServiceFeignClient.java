package com.wisewallet.notification.infrastructure.client;

import com.wisewallet.notification.infrastructure.client.config.AccountServiceFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "account-service",
        url = "${account.service.url:http://localhost:8081}",
        configuration = AccountServiceFeignConfig.class
)
public interface AccountServiceFeignClient {

    @GetMapping("/internal/users/{userId}")
    UserInfoResponse getUserInfo(@PathVariable UUID userId);

    record UserInfoResponse(UUID userId, String email, String firstName, String lastName) {}
}
