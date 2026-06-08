package com.cromp.notifications.api.dto.request;

import java.util.List;

public record CreateWebhookSubscriptionRequest(
        String url,
        List<String> eventTypes
) {}
