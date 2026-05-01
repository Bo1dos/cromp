package com.cromp.iam.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OrganizationResponse(
        Long id,
        UUID orgUuid,
        String name,
        Map<String, Object> settings,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}