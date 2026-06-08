package com.cromp.notifications.infrastructure.persistence.mapper;

import com.cromp.notifications.domain.model.WebhookDelivery;
import com.cromp.notifications.domain.model.WebhookDeliveryStatus;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryPersistenceMapper {

    public WebhookDelivery toDomain(WebhookDeliveryJpaEntity entity) {
        if (entity == null) return null;
        return WebhookDelivery.reconstitute(
                entity.getId(), entity.getSubscriptionId(), entity.getNotificationUuid(),
                entity.getRequestUrl(), entity.getResponseCode(),
                WebhookDeliveryStatus.valueOf(entity.getStatus()),
                entity.getAttemptNumber(), entity.getErrorMessage(),
                entity.getAttemptedAt(), entity.getNextRetryAt()
        );
    }

    public WebhookDeliveryJpaEntity toJpa(WebhookDelivery domain) {
        if (domain == null) return null;
        return WebhookDeliveryJpaEntity.builder()
                .id(domain.getId())
                .subscriptionId(domain.getSubscriptionId())
                .notificationUuid(domain.getNotificationUuid())
                .requestUrl(domain.getRequestUrl())
                .responseCode(domain.getResponseCode())
                .status(domain.getStatus().name())
                .attemptNumber(domain.getAttemptNumber())
                .errorMessage(domain.getErrorMessage())
                .attemptedAt(domain.getAttemptedAt())
                .nextRetryAt(domain.getNextRetryAt())
                .build();
    }
}
