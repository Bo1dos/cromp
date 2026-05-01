package com.cromp.iam.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserResponse(
        Long id,
        UUID userUuid,
        String email,
        String firstName,
        String lastName,
        String middleName,
        String displayName,
        Map<String, Object> profile,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {}