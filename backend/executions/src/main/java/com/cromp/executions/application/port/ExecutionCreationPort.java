package com.cromp.executions.application.port;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

// Расширенная версия контракта из jobs — заменяет StubExecutionCreationPort
public interface ExecutionCreationPort {
    UUID createExecution(Long organizationId, Long jobId, Long jobVersionId,
                         String source, Long triggeredBy,
                         Map<String, Object> payload, UUID correlationId,
                         int priority, Instant scheduledAt,
                         String executionPolicySnapshot);
}