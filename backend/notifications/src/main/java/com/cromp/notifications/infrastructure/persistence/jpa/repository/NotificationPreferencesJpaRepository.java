package com.cromp.notifications.infrastructure.persistence.jpa.repository;

import com.cromp.notifications.infrastructure.persistence.jpa.entity.NotificationPreferencesJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferencesJpaRepository
        extends JpaRepository<NotificationPreferencesJpaEntity, Long> {

    Optional<NotificationPreferencesJpaEntity> findByUserId(Long userId);
}
