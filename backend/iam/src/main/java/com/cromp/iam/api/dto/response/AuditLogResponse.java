package com.cromp.iam.api.dto.response;

import java.time.Instant;
import java.util.Map;

public record AuditLogResponse(
        Long id,
        Long organizationId,
        Instant recordedAt,
        Long actorId,
        Map<String, Object> actorSnapshot,
        String action,
        String resourceType,
        Long resourceId,
        Map<String, Object> changesDiff
) {}