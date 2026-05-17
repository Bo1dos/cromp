package com.cromp.iam.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MembershipResponse(
        UUID membershipUuid,
        String roleName,
        Instant joinedAt,
        Instant updatedAt
) {}