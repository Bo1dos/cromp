package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionArtifactJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionArtifactPersistenceMapper {

    public ExecutionArtifactJpaEntity toJpa(ExecutionArtifact a) {
        return ExecutionArtifactJpaEntity.builder()
                .id(a.getId())
                .executionId(a.getExecutionId())
                .kind(a.getKind())
                .storagePath(a.getStoragePath())
                .sizeBytes(a.getSizeBytes())
                .checksumSha256(a.getChecksumSha256())
                .contentType(a.getContentType())
                .compression(a.getCompression())
                .metadata(a.getMetadata())
                .retentionDays(a.getRetentionDays())
                .uploadedBy(a.getUploadedBy())
                .uploadedAt(a.getUploadedAt())
                .build();
    }

    public ExecutionArtifact toDomain(ExecutionArtifactJpaEntity entity) {
        return ExecutionArtifact.reconstitute(
                entity.getId(),
                entity.getExecutionId(),
                entity.getKind(),
                entity.getStoragePath(),
                entity.getSizeBytes(),
                entity.getChecksumSha256(),
                entity.getContentType(),
                entity.getCompression(),
                entity.getMetadata(),
                entity.getRetentionDays(),
                entity.getUploadedBy(),
                entity.getUploadedAt()
        );
    }
}