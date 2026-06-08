package com.cromp.common.event.domain.invitation;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvitationRejectedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID invitationUuid,
        Long rejectedByUserId,
        Long invitedByUserId
) implements DomainEvent {

    public InvitationRejectedEvent {
        eventType = "invitation.rejected";
    }

    public InvitationRejectedEvent(
            UUID invitationUuid,
            Long organizationId,
            Long rejectedByUserId,
            Long invitedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "invitation.rejected",
                Instant.now(),
                organizationId,
                invitationUuid,
                rejectedByUserId,
                invitedByUserId
        );
    }
}
