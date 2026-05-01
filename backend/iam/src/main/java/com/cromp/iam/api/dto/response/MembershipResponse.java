package com.cromp.iam.api.dto.response;

import java.time.Instant;

public record MembershipResponse(
        Long id,
        Long userId,
        Long organizationId,
        Long roleId,
        String roleName,
        Instant joinedAt,
        Instant updatedAt
) {}