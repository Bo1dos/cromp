package com.cromp.secrets.api.dto.response;

import java.time.Instant;

public record SecretVersionResponse(
        int version,
        boolean isActive,
        Instant createdAt,
        Instant deprecatedAt
) {}