package com.cromp.jobs.infrastructure.persistence.jpa.repository;

import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobJpaRepository extends JpaRepository<JobJpaEntity, Long> {
    Optional<JobJpaEntity> findByIdAndOrganizationId(Long id, Long organizationId);
    List<JobJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<JobJpaEntity> findByOrganizationIdAndStatus(Long organizationId, JobStatus status);
    boolean existsByNameAndOrganizationIdAndDeletedAtIsNull(String name, Long organizationId);
}