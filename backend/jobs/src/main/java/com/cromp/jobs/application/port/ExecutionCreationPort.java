package com.cromp.jobs.application.port;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface ExecutionCreationPort {
    /**
     * @return execution UUID
     */
    UUID createExecution(Long organizationId, Long jobId, Long jobVersionId,
                         String source, Long triggeredBy,
                         Map<String, Object> payload, UUID correlationId,
                         int priority, Instant scheduledAt,
                         String executionPolicySnapshot);
}