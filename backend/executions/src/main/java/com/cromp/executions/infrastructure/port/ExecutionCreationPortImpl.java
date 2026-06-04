package com.cromp.executions.infrastructure.port;

import com.cromp.executions.api.dto.request.CreateExecutionRequest;
import com.cromp.executions.application.service.ExecutionService;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Реализует контракты {@code jobs.application.port.ExecutionCreationPort}
 * и {@code executions.application.port.ExecutionCreationPort}.
 * @Primary вытесняет StubExecutionCreationPort из модуля jobs.
 */
@Component
@Primary
@RequiredArgsConstructor
public class ExecutionCreationPortImpl
        implements com.cromp.jobs.application.port.ExecutionCreationPort,
                   com.cromp.executions.application.port.ExecutionCreationPort {

    private final ExecutionService executionService;

    @Override
    public UUID createExecution(Long organizationId, Long jobId, Long jobVersionId,
                                String source, Long triggeredBy,
                                Map<String, Object> payload, UUID correlationId,
                                int priority, Instant scheduledAt,
                                String executionPolicySnapshot) {
        CreateExecutionRequest request = new CreateExecutionRequest(
                organizationId, jobId, jobVersionId,
                priority,
                ExecutionSource.valueOf(source),
                triggeredBy,
                scheduledAt,
                correlationId,
                payload,
                executionPolicySnapshot
        );
        return executionService.createExecution(request);
    }
}