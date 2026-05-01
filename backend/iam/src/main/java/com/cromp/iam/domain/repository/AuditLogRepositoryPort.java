package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.AuditLogEntry;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepositoryPort {
    AuditLogEntry save(AuditLogEntry entry);
    Optional<AuditLogEntry> findById(Long id);
    List<AuditLogEntry> findByOrganizationId(Long organizationId);
    List<AuditLogEntry> findByActorId(Long actorId);
    List<AuditLogEntry> findByOrganizationIdAndResourceTypeAndResourceId(Long organizationId, String resourceType, Long resourceId);
}