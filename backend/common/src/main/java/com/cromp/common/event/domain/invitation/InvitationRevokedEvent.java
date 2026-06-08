package com.cromp.common.event.domain.invitation;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record InvitationRevokedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID invitationUuid,
        Long revokedByUserId,
        String email
) implements DomainEvent {

    public InvitationRevokedEvent {
        eventType = "invitation.revoked";
    }

    public InvitationRevokedEvent(
            UUID invitationUuid,
            Long organizationId,
            Long revokedByUserId,
            String email
    ) {
        this(
                UUID.randomUUID(),
                "invitation.revoked",
                Instant.now(),
                organizationId,
                invitationUuid,
                revokedByUserId,
                email
        );
    }
}
