package com.cromp.secrets.api.dto.response;

import java.time.Instant;

public record SecretVersionResponse(
        Long id,
        Long secretId,
        int version,
        String keyId,
        boolean active,
        Long createdBy,
        Instant createdAt,
        Instant deprecatedAt
) {}
