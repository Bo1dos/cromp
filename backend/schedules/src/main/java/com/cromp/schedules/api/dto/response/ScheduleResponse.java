package com.cromp.schedules.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Ответ с данными cron-расписания")
public record ScheduleResponse(
        @Schema(description = "Cron-выражение", example = "0 0 * * *")
        String cronExpression,
        @Schema(description = "Таймзона", example = "Europe/Moscow")
        String timezone,
        @Schema(description = "Дополнительные правила (JSON)", nullable = true)
        String rules,
        @Schema(description = "Время следующего запуска")
        Instant nextRunAt,
        @Schema(description = "Статус расписания: ACTIVE, PAUSED")
        String status,
        @Schema(description = "Дата создания")
        Instant createdAt,
        @Schema(description = "Дата обновления")
        Instant updatedAt
) {}