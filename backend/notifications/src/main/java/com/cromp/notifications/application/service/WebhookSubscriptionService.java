package com.cromp.notifications.application.service;

import com.cromp.notifications.api.dto.request.CreateWebhookSubscriptionRequest;
import com.cromp.notifications.api.dto.response.WebhookDeliveryResponse;
import com.cromp.notifications.api.dto.response.WebhookSubscriptionResponse;
import com.cromp.notifications.api.service.WebhookSubscriptionFacade;
import com.cromp.notifications.domain.model.NotificationEventType;
import com.cromp.notifications.domain.model.WebhookDelivery;
import com.cromp.notifications.domain.model.WebhookSubscription;
import com.cromp.notifications.domain.repository.WebhookDeliveryRepositoryPort;
import com.cromp.notifications.domain.repository.WebhookSubscriptionRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WebhookSubscriptionService implements WebhookSubscriptionFacade {

    private final WebhookSubscriptionRepositoryPort subscriptionRepository;
    private final WebhookDeliveryRepositoryPort deliveryRepository;

    public WebhookSubscriptionService(WebhookSubscriptionRepositoryPort subscriptionRepository,
                                       WebhookDeliveryRepositoryPort deliveryRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookSubscriptionResponse> listByOrg(Long orgId) {
        return subscriptionRepository.findByOrganizationId(orgId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public WebhookSubscriptionResponse create(Long orgId, CreateWebhookSubscriptionRequest request) {
        List<NotificationEventType> types = request.eventTypes().stream()
                .map(NotificationEventType::valueOf)
                .toList();

        WebhookSubscription sub = WebhookSubscription.create(orgId, request.url(), types);
        sub = subscriptionRepository.save(sub);

        return new WebhookSubscriptionResponse(
                sub.getSubscriptionUuid(), sub.getUrl(), sub.isEnabled(),
                sub.getEventTypes().stream().map(Enum::name).toList(),
                sub.getSecret(), sub.getCreatedAt()
        );
    }

    @Override
    public WebhookSubscriptionResponse update(Long orgId, UUID subscriptionUuid,
                                               CreateWebhookSubscriptionRequest request) {
        WebhookSubscription sub = subscriptionRepository.findBySubscriptionUuid(subscriptionUuid)
                .orElseThrow(() -> new RuntimeException("Webhook subscription not found"));

        List<NotificationEventType> types = request.eventTypes().stream()
                .map(NotificationEventType::valueOf)
                .toList();
        sub.update(request.url(), true, types);
        sub = subscriptionRepository.save(sub);

        return toResponse(sub);
    }

    @Override
    public void delete(Long orgId, UUID subscriptionUuid) {
        WebhookSubscription sub = subscriptionRepository.findBySubscriptionUuid(subscriptionUuid)
                .orElseThrow(() -> new RuntimeException("Webhook subscription not found"));
        subscriptionRepository.delete(sub);
    }

    @Override
    public WebhookSubscriptionResponse rotateSecret(Long orgId, UUID subscriptionUuid) {
        WebhookSubscription sub = subscriptionRepository.findBySubscriptionUuid(subscriptionUuid)
                .orElseThrow(() -> new RuntimeException("Webhook subscription not found"));
        String newSecret = sub.rotateSecret();
        sub = subscriptionRepository.save(sub);

        return new WebhookSubscriptionResponse(
                sub.getSubscriptionUuid(), sub.getUrl(), sub.isEnabled(),
                sub.getEventTypes().stream().map(Enum::name).toList(),
                newSecret, sub.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookDeliveryResponse> getDeliveries(Long orgId, UUID subscriptionUuid, int page, int size) {
        WebhookSubscription sub = subscriptionRepository.findBySubscriptionUuid(subscriptionUuid)
                .orElseThrow(() -> new RuntimeException("Webhook subscription not found"));

        return deliveryRepository.findBySubscriptionId(sub.getId(), PageRequest.of(page, size)).stream()
                .map(d -> new WebhookDeliveryResponse(
                        d.getResponseCode(), d.getStatus().name(),
                        d.getAttemptNumber(), d.getErrorMessage(), d.getAttemptedAt()))
                .toList();
    }

    private WebhookSubscriptionResponse toResponse(WebhookSubscription sub) {
        return new WebhookSubscriptionResponse(
                sub.getSubscriptionUuid(), sub.getUrl(), sub.isEnabled(),
                sub.getEventTypes().stream().map(Enum::name).toList(),
                null, sub.getCreatedAt()
        );
    }
}
