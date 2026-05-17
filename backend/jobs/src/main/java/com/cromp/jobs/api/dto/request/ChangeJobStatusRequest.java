package com.cromp.jobs.api.dto.request;

import com.cromp.jobs.domain.model.enums.JobStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeJobStatusRequest(
        @NotNull JobStatus status
) {}