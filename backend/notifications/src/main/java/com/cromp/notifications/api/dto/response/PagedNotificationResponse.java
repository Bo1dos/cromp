package com.cromp.notifications.api.dto.response;

import java.util.List;

public record PagedNotificationResponse(
        List<NotificationResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
