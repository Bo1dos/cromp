package com.cromp.notifications.domain.repository;

import com.cromp.notifications.domain.model.Notification;
import com.cromp.notifications.domain.model.NotificationStatus;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    Optional<Notification> findByNotificationUuid(UUID notificationUuid);

    java.util.List<Notification> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    java.util.List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status,
                                                        org.springframework.data.domain.Pageable pageable);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);

    void markAllReadByUserId(Long userId);
}
