package com.cromp.notifications.infrastructure.persistence.mapper;

import com.cromp.notifications.domain.model.Notification;
import com.cromp.notifications.domain.model.NotificationChannel;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.NotificationStatus;
import com.cromp.notifications.infrastructure.persistence.jpa.entity.NotificationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationPersistenceMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) return null;
        return Notification.reconstitute(
                entity.getId(),
                entity.getNotificationUuid(),
                entity.getUserId(),
                entity.getOrganizationId(),
                NotificationEventType.valueOf(entity.getEventType()),
                NotificationChannel.valueOf(entity.getChannel()),
                entity.getTitle(),
                entity.getBody(),
                entity.getMetadataJson(),
                NotificationStatus.valueOf(entity.getStatus()),
                entity.getEmailSentTo(),
                entity.getEmailSentAt(),
                entity.getReadAt(),
                entity.getCreatedAt()
        );
    }

    public NotificationJpaEntity toJpa(Notification domain) {
        if (domain == null) return null;
        return NotificationJpaEntity.builder()
                .id(domain.getId())
                .notificationUuid(domain.getNotificationUuid())
                .userId(domain.getUserId())
                .organizationId(domain.getOrganizationId())
                .eventType(domain.getEventType().name())
                .channel(domain.getChannel().name())
                .title(domain.getTitle())
                .body(domain.getBody())
                .metadataJson(domain.getMetadataJson())
                .status(domain.getStatus().name())
                .emailSentTo(domain.getEmailSentTo())
                .emailSentAt(domain.getEmailSentAt())
                .readAt(domain.getReadAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
