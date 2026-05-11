package com.cromp.jobs.api.dto.request;

import com.cromp.jobs.domain.model.JobConfig;

public record UpdateJobRequest(
        String name,
        String description,
        JobConfig config,
        String queueName,
        Integer priority
) {}