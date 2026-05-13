package com.cromp.executions.api.mapper;

import com.cromp.executions.api.dto.response.ExecutionArtifactResponse;
import com.cromp.executions.api.dto.response.ExecutionAttemptResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.ExecutionAttempt;
import org.springframework.stereotype.Component;

@Component
public class ExecutionApiMapper {
    public ExecutionResponse toResponse(Execution execution) {
        return new ExecutionResponse(
                execution.getId(),
                execution.getExecUuid(),
                execution.getOrganizationId(),
                execution.getJobId(),
                execution.getJobVersionId(),
                execution.getPriority(),
                execution.getSource().name(),
                execution.getTriggeredAt(),
                execution.getScheduledAt(),
                execution.getFinalStatus().name(),
                execution.getTotalAttempts(),
                execution.getStartedAt(),
                execution.getFinishedAt(),
                execution.getCorrelationId(),
                execution.getExecutionPolicySnapshot(),
                execution.getCreatedAt(),
                execution.getUpdatedAt()
        );
    }

    public ExecutionAttemptResponse toAttemptResponse(ExecutionAttempt attempt) {
        return new ExecutionAttemptResponse(
                attempt.getId(),
                attempt.getAttemptUuid(),
                attempt.getExecutionId(),
                attempt.getOrganizationId(),
                attempt.getAttemptNumber(),
                attempt.getStatus().name(),
                attempt.getStatusReason(),
                attempt.getErrorClass(),
                attempt.getScheduledAt(),
                attempt.getClaimedAt(),
                attempt.getStartedAt(),
                attempt.getFinishedAt(),
                attempt.getDurationMs(),
                attempt.getExecutorMetadata(),
                attempt.getIdempotencyKey(),
                attempt.getOutputSummary(),
                attempt.getTraceId(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt()
        );
    }

    public ExecutionArtifactResponse toArtifactResponse(ExecutionArtifact artifact) {
        return new ExecutionArtifactResponse(
                artifact.getId(),
                artifact.getExecutionId(),
                artifact.getKind().name(),
                artifact.getStoragePath(),
                artifact.getSizeBytes(),
                artifact.getChecksumSha256(),
                artifact.getContentType(),
                artifact.getCompression(),
                artifact.getMetadata(),
                artifact.getRetentionDays(),
                artifact.getUploadedBy(),
                artifact.getUploadedAt()
        );
    }
}
