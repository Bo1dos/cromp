package com.cromp.notifications.infrastructure.persistence.jpa.repository;

import com.cromp.notifications.infrastructure.persistence.jpa.entity.WebhookDeliveryJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface WebhookDeliveryJpaRepository
        extends JpaRepository<WebhookDeliveryJpaEntity, Long> {

    Page<WebhookDeliveryJpaEntity> findBySubscriptionId(Long subscriptionId, Pageable pageable);

    List<WebhookDeliveryJpaEntity> findByStatusAndNextRetryAtBefore(String status, Instant before);
}
