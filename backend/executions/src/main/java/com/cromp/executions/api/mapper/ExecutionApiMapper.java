package com.cromp.executions.api.mapper;

import com.cromp.executions.api.dto.response.ArtifactResponse;
import com.cromp.executions.api.dto.response.AttemptResponse;
import com.cromp.executions.api.dto.response.ExecutionDetailResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.ExecutionAttempt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionApiMapper {

    public ExecutionResponse toResponse(Execution e) {
        return new ExecutionResponse(
                e.getExecUuid(),
                e.getJobId(),
                e.getJobVersionId(),
                e.getPriority(),
                e.getSource().name(),
                e.getFinalStatus().name(),
                e.getTotalAttempts(),
                e.getTriggeredAt(),
                e.getScheduledAt(),
                e.getStartedAt(),
                e.getFinishedAt(),
                e.getCorrelationId(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public ExecutionDetailResponse toDetailResponse(Execution e, List<ExecutionAttempt> attempts) {
        return new ExecutionDetailResponse(
                e.getExecUuid(),
                e.getJobId(),
                e.getJobVersionId(),
                e.getPriority(),
                e.getSource().name(),
                e.getFinalStatus().name(),
                e.getTotalAttempts(),
                e.getTriggeredAt(),
                e.getScheduledAt(),
                e.getStartedAt(),
                e.getFinishedAt(),
                e.getCorrelationId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                attempts.stream().map(this::toAttemptResponse).toList()
        );
    }

    public AttemptResponse toAttemptResponse(ExecutionAttempt a) {
        return new AttemptResponse(
                a.getAttemptUuid(),
                a.getAttemptNumber(),
                a.getStatus().name(),
                a.getStatusReason(),
                a.getErrorClass(),
                a.getScheduledAt(),
                a.getClaimedAt(),
                a.getStartedAt(),
                a.getFinishedAt(),
                a.getDurationMs(),
                a.getTraceId(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    public ArtifactResponse toArtifactResponse(ExecutionArtifact a) {
        return new ArtifactResponse(
                a.getId(),
                a.getKind().name(),
                a.getStoragePath(),
                a.getSizeBytes(),
                a.getContentType(),
                a.getChecksumSha256(),
                a.getRetentionDays(),
                a.getUploadedAt()
        );
    }
}