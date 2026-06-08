package com.cromp.common.event.domain.membership;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MemberAddedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        Long userId,
        String userName,
        String roleName,
        Long addedByUserId
) implements DomainEvent {

    public MemberAddedEvent {
        eventType = "membership.added";
    }

    public MemberAddedEvent(
            Long organizationId,
            Long userId,
            String userName,
            String roleName,
            Long addedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "membership.added",
                Instant.now(),
                organizationId,
                userId,
                userName,
                roleName,
                addedByUserId
        );
    }
}
