package com.cromp.schedules.api.dto.response;

import java.time.Instant;

public record ScheduleResponse(
        String cronExpression,
        String timezone,
        String rules,
        Instant nextRunAt,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}