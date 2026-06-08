package com.cromp.notifications.api.dto.response;

import java.time.Instant;

public record WebhookDeliveryResponse(
        int responseCode,
        String status,
        int attemptNumber,
        String errorMessage,
        Instant attemptedAt
) {}
