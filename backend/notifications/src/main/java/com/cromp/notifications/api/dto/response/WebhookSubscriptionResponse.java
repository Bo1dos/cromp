package com.cromp.notifications.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WebhookSubscriptionResponse(
        UUID subscriptionUuid,
        String url,
        boolean enabled,
        List<String> eventTypes,
        String secret,
        Instant createdAt
) {}
