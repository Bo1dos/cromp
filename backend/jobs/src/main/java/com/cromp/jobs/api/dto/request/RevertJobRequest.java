package com.cromp.jobs.api.dto.request;

import jakarta.validation.constraints.Min;

public record RevertJobRequest(
        @Min(1) int version,
        String reason
) {}