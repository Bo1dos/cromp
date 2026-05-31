package com.cromp.schedules.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание или обновление cron-расписания")
public record CreateUpdateScheduleRequest(
        @NotBlank @Size(max = 128)
        @Schema(description = "Cron-выражение (стандарт Unix cron)", example = "0 0 * * *")
        String cronExpression,
        @NotBlank @Size(max = 64)
        @Schema(description = "Таймзона (IANA)", example = "Europe/Moscow")
        String timezone,
        @Schema(description = "Дополнительные правила (JSON, nullable)", nullable = true)
        String rules
) {}