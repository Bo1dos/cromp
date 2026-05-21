package com.cromp.jobs.api.service;

import com.cromp.jobs.api.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface JobVersionFacade {
    List<JobVersionResponse> getVersions(UUID jobUuid);
    JobVersionResponse getVersion(UUID jobUuid, int version);
    JobVersionComparisonResponse compareVersions(UUID jobUuid, int fromVersion, int toVersion);
    JobVersionResponse revertToVersion(UUID jobUuid, int version);
    JobVersionResponse getCurrentVersion(UUID jobUuid);
}