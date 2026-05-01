package com.cromp.iam.api.dto.response;

import java.time.Instant;

public record InvitationResponse(
        Long id,
        Long organizationId,
        String email,
        Long roleId,
        String roleName,
        Long invitedBy,
        Instant expiresAt,
        Instant createdAt,
        Instant acceptedAt,
        String status,
        String invitationToken
) {}