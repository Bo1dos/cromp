package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record InviteUserRequest(
        @NotNull Long organizationId,
        @Email String email,
        @NotNull Long roleId,
        Long invitedBy,
        Instant expiresAt
) {}