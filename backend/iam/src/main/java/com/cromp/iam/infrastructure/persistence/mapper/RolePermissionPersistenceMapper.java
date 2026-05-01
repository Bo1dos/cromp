package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.infrastructure.persistence.jpa.entity.RolePermissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionPersistenceMapper {

    public RolePermissionJpaEntity toJpa(RolePermission rolePermission) {
        return RolePermissionJpaEntity.builder()
                .id(rolePermission.getId())
                .roleId(rolePermission.getRoleId())
                .permission(rolePermission.getPermission())
                .build();
    }

    public RolePermission toDomain(RolePermissionJpaEntity entity) {
        return RolePermission.reconstitute(
                entity.getId(),
                entity.getRoleId(),
                entity.getPermission()
        );
    }
}