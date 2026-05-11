package com.cromp.jobs.api.service;

import com.cromp.jobs.api.dto.response.*;

import java.util.List;

public interface JobVersionFacade {
    List<JobVersionResponse> getVersions(Long organizationId, Long jobId);
    JobVersionResponse getVersion(Long organizationId, Long jobId, int version);
    JobVersionComparisonResponse compareVersions(Long organizationId, Long jobId, int fromVersion, int toVersion);
    JobVersionResponse revertToVersion(Long organizationId, Long jobId, int version);
    JobVersionResponse getCurrentVersion(Long organizationId, Long jobId);
}