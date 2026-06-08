package com.cromp.notifications.infrastructure.persistence.jpa.repository;

import com.cromp.notifications.infrastructure.persistence.jpa.entity.WebhookSubscriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionJpaRepository
        extends JpaRepository<WebhookSubscriptionJpaEntity, Long> {

    Optional<WebhookSubscriptionJpaEntity> findBySubscriptionUuid(UUID uuid);
    List<WebhookSubscriptionJpaEntity> findByOrganizationId(Long orgId);
}
