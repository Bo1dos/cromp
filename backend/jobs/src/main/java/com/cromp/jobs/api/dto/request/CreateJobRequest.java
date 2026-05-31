package com.cromp.jobs.api.dto.request;

import com.cromp.jobs.domain.model.JobConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

@Schema(description = "Запрос на создание задачи (cron job)")
public record CreateJobRequest(
        @NotBlank
        @Schema(description = "Название задачи", example = "daily-report")
        String name,
        @Schema(description = "Описание задачи", example = "Ежедневная выгрузка отчёта")
        String description,
        @NotNull
        @Schema(description = "Конфигурация задачи (target, retry policy, secrets и т.д.)")
        JobConfig config,
        @Schema(description = "Имя очереди выполнения", example = "default")
        String queueName,
        @Range(min = -100, max = 100)
        @Schema(description = "Приоритет (-100..100, чем выше — тем важнее)", example = "0")
        int priority
) {}