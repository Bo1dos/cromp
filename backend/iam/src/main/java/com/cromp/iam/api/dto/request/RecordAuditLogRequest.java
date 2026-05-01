package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record RecordAuditLogRequest(
        @NotNull Long organizationId,
        Long actorId,
        Map<String, Object> actorSnapshot,
        @NotBlank String action,
        String resourceType,
        Long resourceId,
        Map<String, Object> changesDiff
) {}