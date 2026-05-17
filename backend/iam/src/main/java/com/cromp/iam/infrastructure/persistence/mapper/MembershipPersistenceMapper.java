package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.Membership;
import com.cromp.iam.infrastructure.persistence.jpa.entity.MembershipJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MembershipPersistenceMapper {

    public MembershipJpaEntity toJpa(Membership membership) {
        return MembershipJpaEntity.builder()
                .id(membership.getId())
                .membershipUuid(membership.getMembershipUuid())
                .userId(membership.getUserId())
                .organizationId(membership.getOrganizationId())
                .roleId(membership.getRoleId())
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .deletedAt(membership.getDeletedAt())
                .build();
    }

    public Membership toDomain(MembershipJpaEntity entity) {
        return Membership.reconstitute(
                entity.getId(),
                entity.getMembershipUuid(),
                entity.getUserId(),
                entity.getOrganizationId(),
                entity.getRoleId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}