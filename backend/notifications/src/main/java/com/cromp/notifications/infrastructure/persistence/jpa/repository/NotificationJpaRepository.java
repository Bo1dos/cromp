package com.cromp.notifications.infrastructure.persistence.jpa.repository;

import com.cromp.notifications.infrastructure.persistence.jpa.entity.NotificationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    Optional<NotificationJpaEntity> findByNotificationUuid(UUID notificationUuid);

    Page<NotificationJpaEntity> findByUserId(Long userId, Pageable pageable);

    Page<NotificationJpaEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, String status);

    @Modifying
    @Query("UPDATE NotificationJpaEntity n SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP "
            + "WHERE n.userId = :userId AND n.status = 'UNREAD'")
    void markAllReadByUserId(@Param("userId") Long userId);
}
