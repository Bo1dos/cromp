package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AddRolePermissionRequest;
import com.cromp.iam.api.dto.response.RolePermissionResponse;
import com.cromp.iam.api.service.RolePermissionFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionFacade facade;

    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")  // т.к. управление пермиссиями требует админских прав
    @ResponseStatus(HttpStatus.CREATED)
    public RolePermissionResponse add(@Valid @RequestBody AddRolePermissionRequest request) {
        return facade.add(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id) {
        facade.remove(id);
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public List<RolePermissionResponse> getByRoleId(@PathVariable Long roleId) {
        return facade.getByRoleId(roleId);
    }
}