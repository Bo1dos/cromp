package com.cromp.common.event.domain.membership;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MemberRemovedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        Long userId,
        String userName,
        Long removedByUserId
) implements DomainEvent {

    public MemberRemovedEvent {
        eventType = "membership.removed";
    }

    public MemberRemovedEvent(
            Long organizationId,
            Long userId,
            String userName,
            Long removedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "membership.removed",
                Instant.now(),
                organizationId,
                userId,
                userName,
                removedByUserId
        );
    }
}
