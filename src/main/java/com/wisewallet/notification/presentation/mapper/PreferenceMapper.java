package com.wisewallet.notification.presentation.mapper;

import com.wisewallet.notification.domain.model.NotificationPreference;
import com.wisewallet.notification.presentation.dto.response.PreferenceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PreferenceMapper {

    @Mapping(target = "eventType", expression = "java(preference.getEventType().name())")
    @Mapping(target = "channel", expression = "java(preference.getChannel().name())")
    PreferenceResponse toResponse(NotificationPreference preference);
}
