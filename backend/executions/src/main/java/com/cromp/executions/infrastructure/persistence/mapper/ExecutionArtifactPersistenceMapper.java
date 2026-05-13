package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionArtifactJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionArtifactPersistenceMapper {
    public ExecutionArtifactJpaEntity toJpa(ExecutionArtifact artifact) {
        return ExecutionArtifactJpaEntity.builder()
                .id(artifact.getId())
                .executionId(artifact.getExecutionId())
                .kind(artifact.getKind())
                .storagePath(artifact.getStoragePath())
                .sizeBytes(artifact.getSizeBytes())
                .checksumSha256(artifact.getChecksumSha256())
                .contentType(artifact.getContentType())
                .compression(artifact.getCompression())
                .metadata(artifact.getMetadata())
                .retentionDays(artifact.getRetentionDays())
                .uploadedBy(artifact.getUploadedBy())
                .uploadedAt(artifact.getUploadedAt())
                .build();
    }

    public ExecutionArtifact toDomain(ExecutionArtifactJpaEntity entity) {
        return ExecutionArtifact.builder()
                .id(entity.getId())
                .executionId(entity.getExecutionId())
                .kind(entity.getKind())
                .storagePath(entity.getStoragePath())
                .sizeBytes(entity.getSizeBytes())
                .checksumSha256(entity.getChecksumSha256())
                .contentType(entity.getContentType())
                .compression(entity.getCompression())
                .metadata(entity.getMetadata())
                .retentionDays(entity.getRetentionDays())
                .uploadedBy(entity.getUploadedBy())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}
