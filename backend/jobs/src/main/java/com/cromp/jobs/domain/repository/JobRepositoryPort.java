package com.cromp.jobs.domain.repository;

import com.cromp.jobs.domain.model.Job;
import java.util.List;
import java.util.Optional;

public interface JobRepositoryPort {
    Job save(Job job);
    Optional<Job> findById(Long id);
    Optional<Job> findByIdAndOrganizationId(Long id, Long organizationId);
    List<Job> findByOrganizationId(Long organizationId);
    List<Job> findByOrganizationIdAndStatus(Long organizationId, String status); // JobStatus enum как строка
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
}