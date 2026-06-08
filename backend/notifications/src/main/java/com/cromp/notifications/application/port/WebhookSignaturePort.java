package com.cromp.notifications.application.port;

public interface WebhookSignaturePort {
    String sign(String secret, String payload);
    boolean verify(String secret, String payload, String signature);
}
