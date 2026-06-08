package com.cromp.notifications.domain.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WebhookDelivery {

    private Long id;

    @EqualsAndHashCode.Include
    private Long subscriptionId;

    private UUID notificationUuid;
    private String requestUrl;
    private int responseCode;
    private WebhookDeliveryStatus status;
    private int attemptNumber;
    private String errorMessage;
    private Instant attemptedAt;
    private Instant nextRetryAt;

    private WebhookDelivery(Long id,
                            Long subscriptionId,
                            UUID notificationUuid,
                            String requestUrl,
                            int responseCode,
                            WebhookDeliveryStatus status,
                            int attemptNumber,
                            String errorMessage,
                            Instant attemptedAt,
                            Instant nextRetryAt) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.notificationUuid = notificationUuid;
        this.requestUrl = requestUrl;
        this.responseCode = responseCode;
        this.status = status;
        this.attemptNumber = attemptNumber;
        this.errorMessage = errorMessage;
        this.attemptedAt = attemptedAt;
        this.nextRetryAt = nextRetryAt;
    }

    public static WebhookDelivery createPending(Long subscriptionId,
                                                 UUID notificationUuid,
                                                 String requestUrl) {
        return new WebhookDelivery(
                null, subscriptionId, notificationUuid, requestUrl,
                0, WebhookDeliveryStatus.PENDING, 1, null,
                Instant.now(), null
        );
    }

    public static WebhookDelivery reconstitute(Long id,
                                                Long subscriptionId,
                                                UUID notificationUuid,
                                                String requestUrl,
                                                int responseCode,
                                                WebhookDeliveryStatus status,
                                                int attemptNumber,
                                                String errorMessage,
                                                Instant attemptedAt,
                                                Instant nextRetryAt) {
        return new WebhookDelivery(
                id, subscriptionId, notificationUuid, requestUrl,
                responseCode, status, attemptNumber, errorMessage,
                attemptedAt, nextRetryAt
        );
    }

    public void markSucceeded(int responseCode) {
        this.status = WebhookDeliveryStatus.SUCCEEDED;
        this.responseCode = responseCode;
        this.attemptedAt = Instant.now();
        this.nextRetryAt = null;
    }

    public void markFailed(int responseCode, String errorMessage) {
        this.status = WebhookDeliveryStatus.FAILED;
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.attemptedAt = Instant.now();
    }

    public void scheduleRetry(int nextAttemptNumber, Instant nextRetryAt) {
        this.status = WebhookDeliveryStatus.PENDING;
        this.attemptNumber = nextAttemptNumber;
        this.nextRetryAt = nextRetryAt;
    }
}
