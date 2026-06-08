package com.cromp.notifications.api.dto.request;

import java.util.Map;

public record UpdateNotificationPreferencesRequest(
        Map<String, Boolean> channels,
        Map<String, Boolean> eventTypes
) {}
