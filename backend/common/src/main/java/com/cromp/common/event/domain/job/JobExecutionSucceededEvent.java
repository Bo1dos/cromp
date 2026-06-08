package com.cromp.common.event.domain.job;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record JobExecutionSucceededEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID jobUuid,
        String jobName,
        UUID executionUuid,
        Long durationMs
) implements DomainEvent {

    public JobExecutionSucceededEvent {
        eventType = "job.execution.succeeded";
    }

    public JobExecutionSucceededEvent(
            UUID jobUuid,
            String jobName,
            UUID executionUuid,
            Long organizationId,
            Long durationMs
    ) {
        this(
                UUID.randomUUID(),
                "job.execution.succeeded",
                Instant.now(),
                organizationId,
                jobUuid,
                jobName,
                executionUuid,
                durationMs
        );
    }
}
