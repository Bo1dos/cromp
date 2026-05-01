package com.cromp.iam.api.dto.response;

public record RolePermissionResponse(
        Long id,
        Long roleId,
        String permission
) {}