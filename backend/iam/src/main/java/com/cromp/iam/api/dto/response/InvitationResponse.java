package com.cromp.iam.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID invitationUuid,
        String email,
        String roleName,
        Instant expiresAt,
        Instant createdAt,
        Instant acceptedAt,
        String status
) {}