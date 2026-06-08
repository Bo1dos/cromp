package com.cromp.notifications.application.port;

import com.cromp.notifications.domain.model.WebhookDelivery;

public interface WebhookDispatcherPort {
    WebhookDelivery dispatch(String url, String secret, String payloadJson);
}
