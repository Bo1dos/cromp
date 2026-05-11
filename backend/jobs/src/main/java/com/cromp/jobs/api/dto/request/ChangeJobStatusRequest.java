package com.cromp.jobs.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeJobStatusRequest(
        @NotNull String status // ACTIVE or DISABLED
) {}