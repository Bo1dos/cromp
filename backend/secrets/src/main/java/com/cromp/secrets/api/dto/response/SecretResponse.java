package com.cromp.secrets.api.dto.response;

import java.time.Instant;
import java.util.UUID;

// TODO: рефактор, Long id - это конечно...
public record SecretResponse(
        Long id,
        UUID secretUuid,
        Long organizationId,
        String name,
        String scope,
        String description,
        int currentVersion,
        Instant createdAt,
        Instant updatedAt
) {}