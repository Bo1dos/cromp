package com.cromp.secrets.api.dto.response;

import java.time.Instant;

public record SecretVersionResponse(
        Long id,
        int version,
        boolean isActive,
        Long createdBy,
        Instant createdAt,
        Instant deprecatedAt
) {}