package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.AddRolePermissionRequest;
import com.cromp.iam.api.dto.response.RolePermissionResponse;
import com.cromp.iam.api.mapper.RolePermissionApiMapper;
import com.cromp.iam.api.service.RolePermissionFacade;
import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.iam.domain.repository.RolePermissionRepositoryPort;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionApplicationService implements RolePermissionFacade {

    private final RolePermissionRepositoryPort rolePermissionRepository;
    private final RoleRepositoryPort roleRepository;
    private final RolePermissionApiMapper mapper;

    @Override
    public RolePermissionResponse add(AddRolePermissionRequest request) {
        roleRepository.findById(request.roleId())
                .orElseThrow(() -> new DomainException("Role not found"));
        RolePermission rp = RolePermission.of(request.roleId(), request.permission());
        rp = rolePermissionRepository.save(rp);
        return mapper.toResponse(rp);
    }

    @Override
    public void remove(Long rolePermissionId) {
        rolePermissionRepository.deleteById(rolePermissionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionResponse> getByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}