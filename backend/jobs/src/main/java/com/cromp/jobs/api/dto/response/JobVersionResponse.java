package com.cromp.jobs.api.dto.response;

import java.time.Instant;

public record JobVersionResponse(
        Long id,
        Long jobId,
        int version,
        JobConfigResponse config,
        Long createdBy,
        Instant createdAt
) {}