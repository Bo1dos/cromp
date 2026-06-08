package com.cromp.notifications.domain.model.exceptions;

public class WebhookDeliveryException extends RuntimeException {
    public WebhookDeliveryException(String message) {
        super(message);
    }
}
