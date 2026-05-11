package com.wisewallet.notification.infrastructure.sqs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisewallet.notification.application.port.out.AccountServicePort;
import com.wisewallet.notification.application.port.out.EmailSenderPort;
import com.wisewallet.notification.application.port.out.UserInfo;
import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.domain.model.NotificationStatus;
import com.wisewallet.notification.domain.repository.NotificationRepositoryPort;
import com.wisewallet.notification.infrastructure.email.ThymeleafTemplateRenderer;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSqsWorker {

    private final NotificationRepositoryPort notificationRepository;
    private final AccountServicePort accountServicePort;
    private final ThymeleafTemplateRenderer templateRenderer;
    private final EmailSenderPort emailSenderPort;
    private final ObjectMapper objectMapper;

    @SqsListener("wisewallet-email")
    @Transactional
    public void processEmailMessage(String messageBody) {
        Map<String, String> msg = deserialize(messageBody);
        UUID notificationId = UUID.fromString(msg.get("notificationId"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() != NotificationStatus.PENDING) {
            log.debug("Skipping non-PENDING notification: id={} status={}", notificationId, notification.getStatus());
            return;
        }

        try {
            UserInfo userInfo = accountServicePort.getUserInfo(notification.getUserId());

            Map<String, Object> variables = buildVariables(notification, userInfo);
            String templateName = "email/" + notification.getEventType().name().toLowerCase().replace("_", "-");
            String htmlBody = templateRenderer.render(templateName, variables);

            String emailAddress = userInfo.email();
            if (emailAddress == null) {
                log.warn("No email address for userId={}, skipping email", notification.getUserId());
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
                return;
            }

            emailSenderPort.send(emailAddress, notification.getTitle(), htmlBody);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

            log.debug("Email sent for notificationId={}", notificationId);
        } catch (Exception e) {
            log.error("Email dispatch failed for notificationId={}: {}", notificationId, e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            throw e;
        }
    }

    private Map<String, Object> buildVariables(Notification notification, UserInfo userInfo) {
        Map<String, Object> vars = new HashMap<>();
        if (notification.getMetadata() != null) {
            vars.putAll(notification.getMetadata());
        }
        vars.put("notificationId", notification.getId());
        vars.put("title", notification.getTitle());
        vars.put("body", notification.getBody());
        vars.put("userName", userInfo.firstName() != null
                ? userInfo.firstName() + " " + userInfo.lastName()
                : "User");
        vars.put("userEmail", userInfo.email());
        return vars;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> deserialize(String body) {
        try {
            // SQS message wraps SNS notification — extract actual message
            Map<String, Object> outer = objectMapper.readValue(body, new TypeReference<>() {});
            if (outer.containsKey("Message")) {
                return objectMapper.readValue((String) outer.get("Message"), new TypeReference<>() {});
            }
            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize SQS message: " + body, e);
        }
    }
}
