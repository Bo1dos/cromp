package com.cromp.iam.api.dto.request;

import com.cromp.iam.domain.model.enums.UserRole;

import jakarta.validation.constraints.NotNull;

public record ChangeMembershipRoleRequest(
        @NotNull UserRole role
) {}