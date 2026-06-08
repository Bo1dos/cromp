package com.cromp.common.event.domain.invitation;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvitationCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID invitationUuid,
        String email,
        String roleName,
        Long invitedByUserId,
        String invitedByName
) implements DomainEvent {

    public InvitationCreatedEvent {
        eventType = "invitation.created";
    }

    public InvitationCreatedEvent(
            UUID invitationUuid,
            Long organizationId,
            String email,
            String roleName,
            Long invitedByUserId,
            String invitedByName
    ) {
        this(
                UUID.randomUUID(),
                "invitation.created",
                Instant.now(),
                organizationId,
                invitationUuid,
                email,
                roleName,
                invitedByUserId,
                invitedByName
        );
    }
}
