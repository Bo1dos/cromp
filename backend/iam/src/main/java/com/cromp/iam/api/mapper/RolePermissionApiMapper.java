package com.cromp.iam.api.mapper;

import com.cromp.iam.api.dto.response.RolePermissionResponse;
import com.cromp.iam.domain.model.RolePermission;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionApiMapper {

    public RolePermissionResponse toResponse(RolePermission rolePermission) {
        return new RolePermissionResponse(
                rolePermission.getId(),
                rolePermission.getRoleId(),
                rolePermission.getPermission()
        );
    }
}