package com.cromp.notifications.domain.model;

import com.cromp.notifications.domain.model.support.DomainChecks;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WebhookSubscription {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private Long id;

    @EqualsAndHashCode.Include
    private UUID subscriptionUuid;

    private Long organizationId;
    private String url;
    private String secret;
    private boolean enabled;
    private List<NotificationEventType> eventTypes;
    private Instant createdAt;
    private Instant updatedAt;

    private WebhookSubscription(Long id,
                                UUID subscriptionUuid,
                                Long organizationId,
                                String url,
                                String secret,
                                boolean enabled,
                                List<NotificationEventType> eventTypes,
                                Instant createdAt,
                                Instant updatedAt) {
        this.id = id;
        this.subscriptionUuid = subscriptionUuid == null ? UUID.randomUUID() : subscriptionUuid;
        this.organizationId = DomainChecks.requireNonNullValue(organizationId, "organizationId");
        this.url = DomainChecks.requireText(url, "url");
        this.secret = DomainChecks.requireText(secret, "secret");
        this.enabled = enabled;
        this.eventTypes = eventTypes == null ? List.of() : new ArrayList<>(eventTypes);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public static WebhookSubscription create(Long organizationId,
                                              String url,
                                              List<NotificationEventType> eventTypes) {
        return new WebhookSubscription(
                null,
                UUID.randomUUID(),
                organizationId,
                url,
                generateSecret(),
                true,
                eventTypes,
                Instant.now(),
                Instant.now()
        );
    }

    public static WebhookSubscription reconstitute(Long id,
                                                    UUID subscriptionUuid,
                                                    Long organizationId,
                                                    String url,
                                                    String secret,
                                                    boolean enabled,
                                                    List<NotificationEventType> eventTypes,
                                                    Instant createdAt,
                                                    Instant updatedAt) {
        return new WebhookSubscription(
                id, subscriptionUuid, organizationId, url, secret,
                enabled, eventTypes, createdAt, updatedAt
        );
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public String rotateSecret() {
        this.secret = generateSecret();
        this.updatedAt = Instant.now();
        return this.secret;
    }

    public void update(String url, boolean enabled, List<NotificationEventType> eventTypes) {
        this.url = DomainChecks.requireText(url, "url");
        this.enabled = enabled;
        this.eventTypes = eventTypes == null ? List.of() : new ArrayList<>(eventTypes);
        this.updatedAt = Instant.now();
    }

    private static String generateSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
