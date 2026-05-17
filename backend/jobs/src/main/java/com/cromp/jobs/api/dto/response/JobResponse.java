package com.cromp.jobs.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record JobResponse(
        UUID jobUuid,
        String name,
        String description,
        String status,
        String queueName,
        int priority,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        JobConfigResponse currentConfig,
        int currentVersion,
        int versionCount
) {}