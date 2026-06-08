package com.cromp.common.event.domain.secret;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SecretExpiringEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID secretUuid,
        String secretName,
        Instant expiresAt,
        Long daysRemaining
) implements DomainEvent {

    public SecretExpiringEvent {
        eventType = "secret.expiring";
    }

    public SecretExpiringEvent(
            UUID secretUuid,
            String secretName,
            Long organizationId,
            Instant expiresAt,
            Long daysRemaining
    ) {
        this(
                UUID.randomUUID(),
                "secret.expiring",
                Instant.now(),
                organizationId,
                secretUuid,
                secretName,
                expiresAt,
                daysRemaining
        );
    }
}
