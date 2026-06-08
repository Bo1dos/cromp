package com.cromp.common.event.domain.job;

import com.cromp.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record JobExecutionFailedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        Long organizationId,
        UUID jobUuid,
        String jobName,
        UUID executionUuid,
        String errorMessage,
        Instant failedAt
) implements DomainEvent {

    public JobExecutionFailedEvent {
        eventType = "job.execution.failed";
    }

    public JobExecutionFailedEvent(
            UUID jobUuid,
            String jobName,
            UUID executionUuid,
            Long organizationId,
            String errorMessage
    ) {
        this(
                UUID.randomUUID(),
                "job.execution.failed",
                Instant.now(),
                organizationId,
                jobUuid,
                jobName,
                executionUuid,
                errorMessage,
                Instant.now()
        );
    }
}
