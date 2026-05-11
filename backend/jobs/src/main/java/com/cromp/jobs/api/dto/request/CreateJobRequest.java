package com.cromp.jobs.api.dto.request;

import com.cromp.jobs.domain.model.JobConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record CreateJobRequest(
        @NotBlank String name,
        String description,
        @NotNull JobConfig config,
        String queueName,
        @Range(min = -100, max = 100) int priority
) {}