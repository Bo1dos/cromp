package com.cromp.notifications.api.dto.response;

import java.util.Map;

public record NotificationPreferencesResponse(
        Map<String, Boolean> channels,
        Map<String, Boolean> eventTypes
) {}
