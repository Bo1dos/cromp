package com.cromp.iam.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddRolePermissionRequest(
        @NotNull Long roleId,
        @NotBlank String permission
) {}