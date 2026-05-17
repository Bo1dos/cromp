package com.cromp.jobs.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record JobVersionResponse(
        UUID jobUuid,      
        int version,
        JobConfigResponse config,
        Instant createdAt
) {}