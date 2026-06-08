package com.cromp.notifications.api.service;

import com.cromp.notifications.api.dto.request.UpdateNotificationPreferencesRequest;
import com.cromp.notifications.api.dto.response.NotificationPreferencesResponse;

public interface NotificationPreferencesFacade {
    NotificationPreferencesResponse get(Long userId);
    NotificationPreferencesResponse update(Long userId, UpdateNotificationPreferencesRequest request);
}
