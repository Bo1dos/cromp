package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.infrastructure.persistence.jpa.entity.InvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationJpaRepository extends JpaRepository<InvitationJpaEntity, Long> {
    Optional<InvitationJpaEntity> findByInvitationUuid(UUID invitationUuid);
    Optional<InvitationJpaEntity> findByTokenHash(String tokenHash);
    List<InvitationJpaEntity> findByOrganizationId(Long organizationId);
    List<InvitationJpaEntity> findByOrganizationIdAndStatus(Long organizationId, InvitationStatus status);
    List<InvitationJpaEntity> findByEmail(String email);
}
