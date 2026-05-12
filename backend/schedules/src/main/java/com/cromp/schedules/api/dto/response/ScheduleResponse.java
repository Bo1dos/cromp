package com.cromp.schedules.api.dto.response;

import java.time.Instant;

public record ScheduleResponse(
        Long id,
        Long jobId,
        String cronExpression,
        String timezone,
        String rules,
        Instant nextRunAt,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}