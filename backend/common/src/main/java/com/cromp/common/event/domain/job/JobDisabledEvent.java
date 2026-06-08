package com.cromp.common.event.domain.job;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record JobDisabledEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID jobUuid,
        String jobName,
        Long disabledByUserId
) implements DomainEvent {

    public JobDisabledEvent {
        eventType = "job.disabled";
    }

    public JobDisabledEvent(
            UUID jobUuid,
            String jobName,
            Long organizationId,
            Long disabledByUserId
    ) {
        this(
                UUID.randomUUID(),
                "job.disabled",
                Instant.now(),
                organizationId,
                jobUuid,
                jobName,
                disabledByUserId
        );
    }
}
