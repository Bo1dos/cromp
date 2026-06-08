package com.cromp.common.event.domain.job;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record JobExecutionTimeoutEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID jobUuid,
        String jobName,
        UUID executionUuid,
        Long timeoutMs
) implements DomainEvent {

    public JobExecutionTimeoutEvent {
        eventType = "job.execution.timeout";
    }

    public JobExecutionTimeoutEvent(
            UUID jobUuid,
            String jobName,
            UUID executionUuid,
            Long organizationId,
            Long timeoutMs
    ) {
        this(
                UUID.randomUUID(),
                "job.execution.timeout",
                Instant.now(),
                organizationId,
                jobUuid,
                jobName,
                executionUuid,
                timeoutMs
        );
    }
}
