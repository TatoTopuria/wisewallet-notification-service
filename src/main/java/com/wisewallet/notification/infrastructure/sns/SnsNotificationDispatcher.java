package com.wisewallet.notification.infrastructure.sns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SnsNotificationDispatcher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${wisewallet.notification.sns.topic-arn:arn:aws:sns:us-east-1:000000000000:wisewallet-notifications}")
    private String topicArn;

    public void publish(Notification notification) {
        String messageBody = buildMessageBody(notification);
        String channelValue = notification.getChannel().name();
        String eventTypeValue = notification.getEventType().name();

        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(messageBody)
                .messageAttributes(Map.of(
                        "channel", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(channelValue)
                                .build(),
                        "eventType", MessageAttributeValue.builder()
                                .dataType("String")
                                .stringValue(eventTypeValue)
                                .build()
                ))
                .build();

        snsClient.publish(request);
        log.debug("Published to SNS: notificationId={} channel={}", notification.getId(), channelValue);
    }

    private String buildMessageBody(Notification notification) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "notificationId", notification.getId().toString(),
                    "userId", notification.getUserId().toString(),
                    "eventType", notification.getEventType().name(),
                    "channel", notification.getChannel().name()
            ));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize SNS message body", e);
        }
    }
}
