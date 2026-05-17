package com.cromp.jobs.api.mapper;

import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobVersion;
import org.springframework.stereotype.Component;

@Component
public class JobVersionApiMapper {

    public JobVersionResponse toVersionResponse(Job job, JobVersion version) {
        return new JobVersionResponse(
                job.getJobUuid(),
                version.getVersion(),
                toConfigResponse(version.getConfig()),
                version.getCreatedAt()
        );
    }

    // TODO: вынести общий маппинг конфига в отдельный ConfigMapper, когда понадобится в других местах
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