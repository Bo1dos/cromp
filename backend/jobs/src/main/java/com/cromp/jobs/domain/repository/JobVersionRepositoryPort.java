package com.cromp.jobs.domain.repository;

import com.cromp.jobs.domain.model.JobVersion;
import java.util.List;
import java.util.Optional;

public interface JobVersionRepositoryPort {
    JobVersion save(JobVersion version);
    Optional<JobVersion> findById(Long id);
    Optional<JobVersion> findByJobIdAndVersion(Long jobId, int version);
    List<JobVersion> findByJobIdOrderByVersionDesc(Long jobId);
    Optional<JobVersion> findLatestByJobId(Long jobId);
}