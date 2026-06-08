package com.cromp.notifications.application.service;

import com.cromp.notifications.application.port.WebhookDispatcherPort;
import com.cromp.notifications.domain.model.*;
import com.cromp.notifications.domain.repository.WebhookDeliveryRepositoryPort;
import com.cromp.notifications.domain.repository.WebhookSubscriptionRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WebhookDeliveryService {

    private final WebhookSubscriptionRepositoryPort subscriptionRepository;
    private final WebhookDeliveryRepositoryPort deliveryRepository;
    private final WebhookDispatcherPort dispatcherPort;

    private static final int MAX_RETRIES = 4;
    private static final long[] RETRY_DELAYS_MS = {60_000, 300_000, 900_000, 1_800_000};

    public WebhookDeliveryService(WebhookSubscriptionRepositoryPort subscriptionRepository,
                                   WebhookDeliveryRepositoryPort deliveryRepository,
                                   WebhookDispatcherPort dispatcherPort) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryRepository = deliveryRepository;
        this.dispatcherPort = dispatcherPort;
    }

    public void deliver(UUID subscriptionUuid, NotificationEventType eventType, String payloadJson) {
        var subOpt = subscriptionRepository.findBySubscriptionUuid(subscriptionUuid);
        if (subOpt.isEmpty() || !subOpt.get().isEnabled()) return;

        WebhookSubscription sub = subOpt.get();
        if (!sub.getEventTypes().contains(eventType)) return;

        WebhookDelivery delivery = dispatcherPort.dispatch(sub.getUrl(), sub.getSecret(), payloadJson);
        delivery = WebhookDelivery.reconstitute(
                null, sub.getId(), delivery.getNotificationUuid(), delivery.getRequestUrl(),
                delivery.getResponseCode(), delivery.getStatus(), delivery.getAttemptNumber(),
                delivery.getErrorMessage(), delivery.getAttemptedAt(), delivery.getNextRetryAt()
        );

        if (delivery.getStatus() == WebhookDeliveryStatus.FAILED) {
            scheduleRetry(delivery);
        } else {
            deliveryRepository.save(delivery);
        }
    }

    private void scheduleRetry(WebhookDelivery delivery) {
        if (delivery.getAttemptNumber() >= MAX_RETRIES) {
            deliveryRepository.save(delivery);
            return;
        }

        int nextAttempt = delivery.getAttemptNumber() + 1;
        long delayMs = RETRY_DELAYS_MS[Math.min(delivery.getAttemptNumber(), RETRY_DELAYS_MS.length - 1)];
        delivery.scheduleRetry(nextAttempt, Instant.now().plusMillis(delayMs));
        deliveryRepository.save(delivery);
    }
}
