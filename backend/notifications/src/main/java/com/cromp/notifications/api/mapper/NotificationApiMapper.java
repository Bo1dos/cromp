package com.cromp.notifications.api.mapper;

import com.cromp.notifications.api.dto.response.NotificationPreferencesResponse;
import com.cromp.notifications.api.dto.response.NotificationResponse;
import com.cromp.notifications.domain.model.Notification;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.NotificationPreferences;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class NotificationApiMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationUuid(),
                notification.getEventType().name(),
                notification.getChannel().name(),
                notification.getTitle(),
                notification.getBody(),
                notification.getMetadataJson(),
                notification.getStatus().name(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    public NotificationPreferencesResponse toPreferencesResponse(NotificationPreferences preferences) {
        Map<String, Boolean> channels = preferences.getChannels().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Boolean> eventTypes = preferences.getEventTypes().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return new NotificationPreferencesResponse(channels, eventTypes);
    }
}
