package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, Long> {
    List<AuditLogJpaEntity> findByOrganizationIdOrderByRecordedAtDesc(Long organizationId);
    List<AuditLogJpaEntity> findByActorIdOrderByRecordedAtDesc(Long actorId);
    List<AuditLogJpaEntity> findByOrganizationIdAndResourceTypeAndResourceIdOrderByRecordedAtDesc(Long organizationId, String resourceType, Long resourceId);
}