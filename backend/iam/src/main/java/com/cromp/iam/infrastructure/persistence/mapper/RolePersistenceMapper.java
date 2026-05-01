package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.Role;
import com.cromp.iam.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {

    public RoleJpaEntity toJpa(Role role) {
        return RoleJpaEntity.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }

    public Role toDomain(RoleJpaEntity entity) {
        return Role.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }
}