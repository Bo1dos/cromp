package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.MembershipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, Long> {

    Optional<MembershipJpaEntity> findByMembershipUuid(UUID membershipUuid);
    Optional<MembershipJpaEntity> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    List<MembershipJpaEntity> findByOrganizationId(Long organizationId);
    List<MembershipJpaEntity> findByUserId(Long userId);
}
