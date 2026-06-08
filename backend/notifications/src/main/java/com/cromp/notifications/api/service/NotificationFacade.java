package com.cromp.notifications.api.service;

import com.cromp.notifications.api.dto.response.PagedNotificationResponse;
import com.cromp.notifications.api.dto.response.UnreadCountResponse;
import com.cromp.notifications.domain.model.NotificationStatus;

import java.util.UUID;

public interface NotificationFacade {
    PagedNotificationResponse list(Long userId, NotificationStatus status, int page, int size);
    UnreadCountResponse unreadCount(Long userId);
    void markRead(Long userId, UUID notificationUuid);
    void markAllRead(Long userId);
    void delete(Long userId, UUID notificationUuid);
}
