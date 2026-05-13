package com.cromp.executions.infrastructure.port;

import com.cromp.executions.application.service.ExecutionApplicationService;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.jobs.application.port.ExecutionCreationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class ExecutionCreationPortImpl implements ExecutionCreationPort { // TODO: почему ExecutionCreationPort в jobs, а не в executions???
    private final ExecutionApplicationService executionApplicationService;

    @Override
    public UUID createExecution(Long organizationId, Long jobId, Long jobVersionId, String source,
                                Long triggeredBy, Map<String, Object> payload, UUID correlationId) {
        Execution execution = executionApplicationService.createExecutionDomain(
                organizationId,
                jobId,
                jobVersionId,
                0,
                ExecutionSource.valueOf(source),
                null,
                correlationId,
                payload
        );
        return execution.getExecUuid();
    }
}
