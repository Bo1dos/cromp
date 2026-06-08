package com.cromp.common.event.domain.invitation;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvitationAcceptedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID invitationUuid,
        Long acceptedByUserId,
        String acceptedByName,
        Long invitedByUserId
) implements DomainEvent {

    public InvitationAcceptedEvent {
        eventType = "invitation.accepted";
    }

    public InvitationAcceptedEvent(
            UUID invitationUuid,
            Long organizationId,
            Long acceptedByUserId,
            String acceptedByName,
            Long invitedByUserId
    ) {
        this(
                UUID.randomUUID(),
                "invitation.accepted",
                Instant.now(),
                organizationId,
                invitationUuid,
                acceptedByUserId,
                acceptedByName,
                invitedByUserId
        );
    }
}
