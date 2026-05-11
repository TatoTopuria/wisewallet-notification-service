package com.wisewallet.notification.presentation.mapper;

import com.wisewallet.notification.domain.model.Notification;
import com.wisewallet.notification.presentation.dto.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "eventType", expression = "java(notification.getEventType().name())")
    @Mapping(target = "channel", expression = "java(notification.getChannel().name())")
    @Mapping(target = "status", expression = "java(notification.getStatus().name())")
    NotificationResponse toResponse(Notification notification);
}
