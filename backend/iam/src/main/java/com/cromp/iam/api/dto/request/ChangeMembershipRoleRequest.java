package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeMembershipRoleRequest(
        @NotNull Long membershipId,
        @NotNull Long roleId
) {}