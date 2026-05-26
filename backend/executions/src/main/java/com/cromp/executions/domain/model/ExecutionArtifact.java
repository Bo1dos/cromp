package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ArtifactKind;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ExecutionArtifact {

    private Long id;
    private Long executionId;
    private ArtifactKind kind;
    private String storagePath;
    private Long sizeBytes;
    private String checksumSha256;
    private String contentType;
    private String compression;
    private String metadata; // JSONB
    private Integer retentionDays;
    private Long uploadedBy;
    private Instant uploadedAt;

    private ExecutionArtifact() {}

    public static ExecutionArtifact create(
            Long executionId,
            ArtifactKind kind,
            String storagePath,
            Long sizeBytes,
            String checksumSha256,
            String contentType,
            Long uploadedBy
    ) {
        ExecutionArtifact a = new ExecutionArtifact();
        a.executionId = executionId;
        a.kind = kind;
        a.storagePath = storagePath;
        a.sizeBytes = sizeBytes;
        a.checksumSha256 = checksumSha256;
        a.contentType = contentType;
        a.uploadedBy = uploadedBy;
        a.uploadedAt = Instant.now();
        return a;
    }

    public static ExecutionArtifact reconstitute(
            Long id, Long executionId, ArtifactKind kind, String storagePath,
            Long sizeBytes, String checksumSha256, String contentType,
            String compression, String metadata, Integer retentionDays,
            Long uploadedBy, Instant uploadedAt
    ) {
        ExecutionArtifact a = new ExecutionArtifact();
        a.id = id;
        a.executionId = executionId;
        a.kind = kind;
        a.storagePath = storagePath;
        a.sizeBytes = sizeBytes;
        a.checksumSha256 = checksumSha256;
        a.contentType = contentType;
        a.compression = compression;
        a.metadata = metadata;
        a.retentionDays = retentionDays;
        a.uploadedBy = uploadedBy;
        a.uploadedAt = uploadedAt;
        return a;
    }
}