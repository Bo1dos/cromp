package com.cromp.notifications.domain.repository;

import com.cromp.notifications.domain.model.WebhookSubscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookSubscriptionRepositoryPort {

    WebhookSubscription save(WebhookSubscription subscription);

    Optional<WebhookSubscription> findById(Long id);

    Optional<WebhookSubscription> findBySubscriptionUuid(UUID subscriptionUuid);

    List<WebhookSubscription> findByOrganizationId(Long organizationId);

    void delete(WebhookSubscription subscription);
}
