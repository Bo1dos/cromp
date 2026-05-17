package com.cromp.jobs.domain.repository;

import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.enums.JobStatus;

import java.util.List;
import java.util.Optional;

public interface JobRepositoryPort {
    Job save(Job job);
    Optional<Job> findById(Long id);
    Optional<Job> findByIdAndOrganizationId(Long id, Long organizationId);
    List<Job> findByOrganizationId(Long organizationId);
    List<Job> findByOrganizationIdAndStatus(Long organizationId, JobStatus status);
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
}