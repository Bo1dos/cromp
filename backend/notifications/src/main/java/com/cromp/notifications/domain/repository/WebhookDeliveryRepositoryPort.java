package com.cromp.notifications.domain.repository;

import com.cromp.notifications.domain.model.WebhookDelivery;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WebhookDeliveryRepositoryPort {

    WebhookDelivery save(WebhookDelivery delivery);

    Optional<WebhookDelivery> findById(Long id);

    List<WebhookDelivery> findBySubscriptionId(Long subscriptionId,
                                                org.springframework.data.domain.Pageable pageable);

    List<WebhookDelivery> findPendingRetries(Instant before);
}
