package com.cromp.notifications.api.service;

import com.cromp.notifications.api.dto.request.CreateWebhookSubscriptionRequest;
import com.cromp.notifications.api.dto.response.WebhookDeliveryResponse;
import com.cromp.notifications.api.dto.response.WebhookSubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookSubscriptionFacade {
    List<WebhookSubscriptionResponse> listByOrg(Long orgId);
    WebhookSubscriptionResponse create(Long orgId, CreateWebhookSubscriptionRequest request);
    WebhookSubscriptionResponse update(Long orgId, UUID subscriptionUuid, CreateWebhookSubscriptionRequest request);
    void delete(Long orgId, UUID subscriptionUuid);
    WebhookSubscriptionResponse rotateSecret(Long orgId, UUID subscriptionUuid);
    List<WebhookDeliveryResponse> getDeliveries(Long orgId, UUID subscriptionUuid, int page, int size);
}
