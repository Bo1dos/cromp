package com.cromp.notifications.infrastructure.persistence.mapper;

import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.WebhookSubscription;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class WebhookSubscriptionPersistenceMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookSubscription toDomain(WebhookSubscriptionJpaEntity entity) {
        if (entity == null) return null;
        return WebhookSubscription.reconstitute(
                entity.getId(), entity.getSubscriptionUuid(), entity.getOrganizationId(),
                entity.getUrl(), entity.getSecret(), entity.isEnabled(),
                parseEventTypes(entity.getEventTypes()),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    public WebhookSubscriptionJpaEntity toJpa(WebhookSubscription domain) {
        if (domain == null) return null;
        return WebhookSubscriptionJpaEntity.builder()
                .id(domain.getId())
                .subscriptionUuid(domain.getSubscriptionUuid())
                .organizationId(domain.getOrganizationId())
                .url(domain.getUrl())
                .secret(domain.getSecret())
                .enabled(domain.isEnabled())
                .eventTypes(toJson(domain.getEventTypes()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private List<NotificationEventType> parseEventTypes(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            List<String> raw = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return raw.stream().map(NotificationEventType::valueOf).toList();
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String toJson(List<NotificationEventType> types) {
        try {
            return objectMapper.writeValueAsString(types.stream().map(Enum::name).toList());
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
