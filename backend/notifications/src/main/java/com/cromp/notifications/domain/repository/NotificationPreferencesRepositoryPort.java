package com.cromp.notifications.domain.repository;

import com.cromp.notifications.domain.model.NotificationPreferences;

import java.util.Optional;

public interface NotificationPreferencesRepositoryPort {

    NotificationPreferences save(NotificationPreferences preferences);

    Optional<NotificationPreferences> findByUserId(Long userId);
}
