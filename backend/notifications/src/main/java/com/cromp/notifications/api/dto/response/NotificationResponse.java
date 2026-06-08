package com.cromp.notifications.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationUuid,
        String eventType,
        String channel,
        String title,
        String body,
        String metadataJson,
        String status,
        Instant readAt,
        Instant createdAt
) {}
