package com.cromp.jobs.infrastructure.port;

import com.cromp.jobs.application.port.ExecutionCreationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class StubExecutionCreationPort implements ExecutionCreationPort {

    @Override
    public UUID createExecution(Long organizationId, Long jobId, Long jobVersionId,
                                String source, Long triggeredBy, Map<String, Object> payload,
                                UUID correlationId) {
        UUID execId = UUID.randomUUID();
        log.info("Stub: Execution requested for job {} in org {}, execId={}", jobId, organizationId, execId);
        return execId;
    }
}