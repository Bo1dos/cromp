package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.infrastructure.persistence.jpa.entity.InvitationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class InvitationPersistenceMapper {

    public InvitationJpaEntity toJpa(Invitation invitation) {
        return InvitationJpaEntity.builder()
                .id(invitation.getId())
                .organizationId(invitation.getOrganizationId())
                .email(invitation.getEmail())
                .tokenHash(invitation.getTokenHash())
                .roleId(invitation.getRoleId())
                .invitedBy(invitation.getInvitedBy())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .acceptedAt(invitation.getAcceptedAt())
                .status(invitation.getStatus())
                .build();
    }

    public Invitation toDomain(InvitationJpaEntity entity) {
        return Invitation.reconstitute(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getEmail(),
                entity.getTokenHash(),
                entity.getRoleId(),
                entity.getInvitedBy(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getAcceptedAt(),
                entity.getStatus()
        );
    }
}