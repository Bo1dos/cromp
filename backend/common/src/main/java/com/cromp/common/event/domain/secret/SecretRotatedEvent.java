package com.cromp.common.event.domain.secret;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SecretRotatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID secretUuid,
        String secretName,
        Long rotatedByUserId
) implements DomainEvent {

    public SecretRotatedEvent {
        eventType = "secret.rotated";
    }

    public SecretRotatedEvent(
            UUID secretUuid,
            String secretName,
            Long organizationId,
            Long rotatedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "secret.rotated",
                Instant.now(),
                organizationId,
                secretUuid,
                secretName,
                rotatedByUserId
        );
    }
}
