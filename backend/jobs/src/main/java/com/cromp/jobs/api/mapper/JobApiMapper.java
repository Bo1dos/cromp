package com.cromp.jobs.api.mapper;

import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobVersion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobApiMapper {

    public JobResponse toJobResponse(Job job, JobVersion currentVersion, int versionCount) {
        return new JobResponse(
                job.getId(),
                job.getJobUuid(),
                job.getOrganizationId(),
                job.getName(),
                job.getDescription(),
                job.getStatus().name(),
                job.getQueueName(),
                job.getPriority(),
                job.getCreatedBy(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                toConfigResponse(currentVersion != null ? currentVersion.getConfig() : null),
                currentVersion != null ? currentVersion.getVersion() : 0,
                versionCount
        );
    }

    private JobConfigResponse toConfigResponse(JobConfig config) {
        if (config == null) return null;
        return new JobConfigResponse(
                new JobTargetResponse(
                        config.target().type(),
                        config.target().url(),
                        config.target().method(),
                        config.target().headers(),
                        config.target().body()
                ),
                new RetryPolicyResponse(
                        config.retryPolicy().maxAttempts(),
                        config.retryPolicy().backoffMs(),
                        config.retryPolicy().backoffMultiplier(),
                        config.retryPolicy().retryableErrors()
                ),
                config.timeoutMs(),
                config.secrets().stream()
                        .map(s -> new SecretRefResponse(s.secretId(), s.envName()))
                        .toList()
        );
    }
}