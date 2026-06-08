package com.cromp.common.event.domain.invitation;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvitationExpiredEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID invitationUuid,
        String email,
        Long invitedByUserId
) implements DomainEvent {

    public InvitationExpiredEvent {
        eventType = "invitation.expired";
    }

    public InvitationExpiredEvent(
            UUID invitationUuid,
            Long organizationId,
            String email,
            Long invitedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "invitation.expired",
                Instant.now(),
                organizationId,
                invitationUuid,
                email,
                invitedByUserId
        );
    }
}
