package com.cromp.iam.api.service;

import com.cromp.iam.api.dto.request.AddRolePermissionRequest;
import com.cromp.iam.api.dto.response.RolePermissionResponse;

import java.util.List;

public interface RolePermissionFacade {

    RolePermissionResponse add(AddRolePermissionRequest request);
    void remove(Long rolePermissionId);

    List<RolePermissionResponse> getByRoleId(Long roleId);
}