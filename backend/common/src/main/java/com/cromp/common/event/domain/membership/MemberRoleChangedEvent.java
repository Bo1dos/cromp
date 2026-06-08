package com.cromp.common.event.domain.membership;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record MemberRoleChangedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        Long userId,
        String userName,
        String oldRole,
        String newRole,
        Long changedByUserId
) implements DomainEvent {

    public MemberRoleChangedEvent {
        eventType = "membership.roleChanged";
    }

    public MemberRoleChangedEvent(
            Long organizationId,
            Long userId,
            String userName,
            String oldRole,
            String newRole,
            Long changedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "membership.roleChanged",
                Instant.now(),
                organizationId,
                userId,
                userName,
                oldRole,
                newRole,
                changedByUserId
        );
    }
}
