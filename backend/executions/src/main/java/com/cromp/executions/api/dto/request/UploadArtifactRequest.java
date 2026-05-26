package com.cromp.executions.api.dto.request;

import com.cromp.executions.domain.model.enums.ArtifactKind;

public record UploadArtifactRequest(
        ArtifactKind kind,
        String contentType,
        Integer retentionDays       // nullable — использовать дефолт если null
) {}
