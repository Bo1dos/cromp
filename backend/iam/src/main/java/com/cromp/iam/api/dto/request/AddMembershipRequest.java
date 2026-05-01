package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddMembershipRequest(
        @NotNull Long userId,
        @NotNull Long organizationId,
        @NotNull Long roleId
) {}