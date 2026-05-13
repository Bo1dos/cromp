package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ArtifactKind;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
public class ExecutionArtifact {
    private Long id;
    private Long executionId;
    private ArtifactKind kind;
    private String storagePath;
    private Long sizeBytes;
    private String checksumSha256;
    private String contentType;
    private String compression;
    private Map<String, Object> metadata;
    private Integer retentionDays;
    private Long uploadedBy;
    private Instant uploadedAt;
}
