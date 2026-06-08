package com.cromp.notifications.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.NotificationPreferences;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.NotificationPreferencesJpaEntity;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class NotificationPreferencesPersistenceMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationPreferences toDomain(NotificationPreferencesJpaEntity entity) {
        if (entity == null) return null;
        return NotificationPreferences.reconstitute(
                entity.getId(),
                entity.getUserId(),
                parseChannelMap(entity.getChannels()),
                parseEventTypeMap(entity.getEventTypes()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public NotificationPreferencesJpaEntity toJpa(NotificationPreferences domain) {
        if (domain == null) return null;
        return NotificationPreferencesJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .channels(toJsonString(domain.getChannels()))
                .eventTypes(toJsonString(domain.getEventTypes()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Map<NotificationChannel, Boolean> parseChannelMap(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            Map<String, Boolean> raw = objectMapper.readValue(json,
                    new TypeReference<Map<String, Boolean>>() {});
            Map<NotificationChannel, Boolean> result = new HashMap<>();
            raw.forEach((k, v) -> result.put(NotificationChannel.valueOf(k), v));
            return result;
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private Map<NotificationEventType, Boolean> parseEventTypeMap(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            Map<String, Boolean> raw = objectMapper.readValue(json,
                    new TypeReference<Map<String, Boolean>>() {});
            Map<NotificationEventType, Boolean> result = new HashMap<>();
            raw.forEach((k, v) -> result.put(NotificationEventType.valueOf(k), v));
            return result;
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private String toJsonString(Map<?, Boolean> map) {
        try {
            Map<String, Boolean> stringMap = new HashMap<>();
            map.forEach((k, v) -> stringMap.put(k.toString(), v));
            return objectMapper.writeValueAsString(stringMap);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
