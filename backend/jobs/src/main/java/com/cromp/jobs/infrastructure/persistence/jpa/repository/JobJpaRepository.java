package com.cromp.jobs.infrastructure.persistence.jpa.repository;

import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobJpaEntity, Long> {
    Optional<JobJpaEntity> findByIdAndOrganizationIdAndDeletedAtIsNull(Long id, Long organizationId);
    List<JobJpaEntity> findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long organizationId);
    List<JobJpaEntity> findByOrganizationIdAndStatusAndDeletedAtIsNull(Long organizationId, JobStatus status);
    boolean existsByNameAndOrganizationIdAndDeletedAtIsNull(String name, Long organizationId);
    Optional<JobJpaEntity> findByJobUuidAndDeletedAtIsNull(UUID jobUuid);
}
