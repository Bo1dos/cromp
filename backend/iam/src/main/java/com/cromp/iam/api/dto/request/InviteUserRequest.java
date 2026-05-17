package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

import com.cromp.iam.domain.model.enums.UserRole;

public record InviteUserRequest(
        @Email @NotBlank String email,
        @NotNull UserRole role,
        Instant expiresAt
) {}