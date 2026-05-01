package com.cromp.iam.infrastructure.persistence.adapter;

import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.repository.AuditLogRepositoryPort;
import com.cromp.iam.infrastructure.persistence.jpa.repository.AuditLogJpaRepository;
import com.cromp.iam.infrastructure.persistence.mapper.AuditLogPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository repository;
    private final AuditLogPersistenceMapper mapper;

    @Override
    public AuditLogEntry save(AuditLogEntry entry) {
        return mapper.toDomain(repository.save(mapper.toJpa(entry)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLogEntry> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogEntry> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdOrderByRecordedAtDesc(organizationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogEntry> findByActorId(Long actorId) {
        return repository.findByActorIdOrderByRecordedAtDesc(actorId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogEntry> findByOrganizationIdAndResourceTypeAndResourceId(Long organizationId, String resourceType, Long resourceId) {
        return repository.findByOrganizationIdAndResourceTypeAndResourceIdOrderByRecordedAtDesc(organizationId, resourceType, resourceId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}