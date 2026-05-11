package com.wisewallet.notification.presentation.controller;

import com.wisewallet.notification.application.command.NotificationCommandService;
import com.wisewallet.notification.application.query.NotificationQueryService;
import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.presentation.dto.response.NotificationResponse;
import com.wisewallet.notification.presentation.dto.response.UnreadCountResponse;
import com.wisewallet.notification.presentation.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public Page<NotificationResponse> listNotifications(
            @AuthenticationPrincipal String userId,
            Pageable pageable) {
        return notificationQueryService.listNotifications(UUID.fromString(userId), pageable)
                .map(notificationMapper::toResponse);
    }

    @GetMapping("/unread/count")
    public UnreadCountResponse getUnreadCount(@AuthenticationPrincipal String userId) {
        long count = notificationQueryService.countUnread(UUID.fromString(userId));
        return new UnreadCountResponse(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal String userId,
                                            @PathVariable UUID id) {
        notificationCommandService.markAsRead(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal String userId) {
        notificationCommandService.markAllRead(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
