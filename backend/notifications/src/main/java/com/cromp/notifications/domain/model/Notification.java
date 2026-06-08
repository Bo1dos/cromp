package com.cromp.notifications.domain.model;

import com.cromp.notifications.domain.model.exceptions.NotificationNotFoundException;
import com.cromp.notifications.domain.model.support.DomainChecks;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    private Long id;

    @EqualsAndHashCode.Include
    private UUID notificationUuid;

    private Long userId;
    private Long organizationId;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String title;
    private String body;
    private String metadataJson;
    private NotificationStatus status;
    private String emailSentTo;
    private Instant emailSentAt;
    private Instant readAt;
    private Instant createdAt;

    private Notification(Long id,
                         UUID notificationUuid,
                         Long userId,
                         Long organizationId,
                         NotificationEventType eventType,
                         NotificationChannel channel,
                         String title,
                         String body,
                         String metadataJson,
                         NotificationStatus status,
                         String emailSentTo,
                         Instant emailSentAt,
                         Instant readAt,
                         Instant createdAt) {
        this.id = id;
        this.notificationUuid = notificationUuid == null ? UUID.randomUUID() : notificationUuid;
        this.userId = DomainChecks.requireNonNullValue(userId, "userId");
        this.organizationId = organizationId;
        this.eventType = DomainChecks.requireNonNullValue(eventType, "eventType");
        this.channel = DomainChecks.requireNonNullValue(channel, "channel");
        this.title = DomainChecks.requireText(title, "title");
        this.body = body;
        this.metadataJson = metadataJson;
        this.status = DomainChecks.requireNonNullValue(status, "status");
        this.emailSentTo = emailSentTo;
        this.emailSentAt = emailSentAt;
        this.readAt = readAt;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static Notification create(Long userId,
                                      Long organizationId,
                                      NotificationEventType eventType,
                                      NotificationChannel channel,
                                      String title,
                                      String body,
                                      String metadataJson) {
        return new Notification(
                null,
                UUID.randomUUID(),
                userId,
                organizationId,
                eventType,
                channel,
                title,
                body,
                metadataJson,
                NotificationStatus.UNREAD,
                null,
                null,
                null,
                Instant.now()
        );
    }

    public static Notification reconstitute(Long id,
                                            UUID notificationUuid,
                                            Long userId,
                                            Long organizationId,
                                            NotificationEventType eventType,
                                            NotificationChannel channel,
                                            String title,
                                            String body,
                                            String metadataJson,
                                            NotificationStatus status,
                                            String emailSentTo,
                                            Instant emailSentAt,
                                            Instant readAt,
                                            Instant createdAt) {
        return new Notification(
                id,
                notificationUuid,
                userId,
                organizationId,
                eventType,
                channel,
                title,
                body,
                metadataJson,
                status,
                emailSentTo,
                emailSentAt,
                readAt,
                createdAt
        );
    }

    public void markRead() {
        if (this.status == NotificationStatus.ARCHIVED) {
            throw new NotificationNotFoundException("Cannot mark archived notification as read");
        }
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public void archive() {
        this.status = NotificationStatus.ARCHIVED;
    }

    public void markEmailSent(String emailTo) {
        this.emailSentTo = emailTo;
        this.emailSentAt = Instant.now();
    }
}
