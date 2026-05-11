package com.wisewallet.notification.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.wisewallet.notification.infrastructure.persistence.NotificationJpaRepository;
import com.wisewallet.notification.infrastructure.persistence.NotificationPreferenceJpaRepository;
import com.wisewallet.notification.infrastructure.persistence.ProcessedEventJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Base class for all integration tests.
 * Starts Postgres + Kafka via Testcontainers and WireMock for account-service.
 * AWS auto-configuration is disabled via application-test.yml.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    static WireMockServer wireMock = new WireMockServer(0);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        wireMock.start();
        registry.add("account.service.url", wireMock::baseUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        // Disable SNS/SQS so listeners don't fail without LocalStack
        registry.add("spring.cloud.aws.sns.enabled", () -> "false");
        registry.add("spring.cloud.aws.sqs.enabled", () -> "false");
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected NotificationJpaRepository notificationRepository;

    @Autowired
    protected ProcessedEventJpaRepository processedEventRepository;

    @Autowired
    protected NotificationPreferenceJpaRepository preferenceRepository;

    @BeforeEach
    void baseSetUp() {
        WireMock.configureFor(wireMock.port());
    }

    @AfterEach
    void baseTearDown() {
        notificationRepository.deleteAll();
        processedEventRepository.deleteAll();
        preferenceRepository.deleteAll();
        wireMock.resetAll();
    }
}
