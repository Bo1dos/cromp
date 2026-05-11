package com.cromp.jobs.infrastructure.persistence.jpa.repository;

import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobVersionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobVersionJpaRepository extends JpaRepository<JobVersionJpaEntity, Long> {
    Optional<JobVersionJpaEntity> findByJobIdAndVersion(Long jobId, int version);
    List<JobVersionJpaEntity> findByJobIdOrderByVersionDesc(Long jobId);
    Optional<JobVersionJpaEntity> findTopByJobIdOrderByVersionDesc(Long jobId);
}