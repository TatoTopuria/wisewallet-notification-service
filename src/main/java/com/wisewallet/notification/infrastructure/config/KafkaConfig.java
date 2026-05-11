package com.wisewallet.notification.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers:${spring.kafka.bootstrap-servers:localhost:9092}}")
    private String bootstrapServers;

    @Value("${wisewallet.notification.kafka.consumer.account-concurrency:1}")
    private int accountConcurrency;

    @Value("${wisewallet.notification.kafka.consumer.transaction-concurrency:3}")
    private int transactionConcurrency;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> accountEventListenerFactory() {
        return buildFactory(accountConcurrency);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> transactionEventListenerFactory() {
        return buildFactory(transactionConcurrency);
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object> buildFactory(int concurrency) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(defaultErrorHandler());
        return factory;
    }

    private ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.wisewallet.*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private DefaultErrorHandler defaultErrorHandler() {
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(10000L);
        backOff.setMaxElapsedTime(31000L); // ~3 retries
        return new DefaultErrorHandler(backOff);
    }

    // ---- Topic creation ----

    @Bean
    public NewTopic alertTriggeredTopic() {
        return TopicBuilder.name("alert.triggered").partitions(1).replicas(1).build();
    }
}
