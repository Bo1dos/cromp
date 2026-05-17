package com.cromp.secrets.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SecretResponse(
        UUID secretUuid,
        String name,
        String scope,
        String description,
        int currentVersion,
        Instant createdAt,
        Instant updatedAt
) {}