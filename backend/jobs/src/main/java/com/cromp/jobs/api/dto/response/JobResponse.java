package com.cromp.jobs.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ответ с данными задачи")
public record JobResponse(
        @Schema(description = "UUID задачи")
        UUID jobUuid,
        @Schema(description = "Внутренний ID задачи")
        Long id,
        @Schema(description = "Название задачи")
        String name,
        @Schema(description = "Описание")
        String description,
        @Schema(description = "Статус (ACTIVE, PAUSED, DELETED)")
        String status,
        @Schema(description = "Очередь выполнения")
        String queueName,
        @Schema(description = "Приоритет (-100..100)")
        int priority,
        @Schema(description = "ID создателя")
        Long createdBy,
        @Schema(description = "Дата создания")
        Instant createdAt,
        @Schema(description = "Дата обновления")
        Instant updatedAt,
        @Schema(description = "Текущая конфигурация")
        JobConfigResponse currentConfig,
        @Schema(description = "Номер текущей версии")
        int currentVersion,
        @Schema(description = "Общее количество версий")
        int versionCount,
        @Schema(description = "Есть ли расписание")
        boolean hasSchedule
) {}