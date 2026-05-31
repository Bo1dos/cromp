package com.cromp.secrets.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ответ с метаданными секрета. Значение НЕ возвращается.")
public record SecretResponse(
        @Schema(description = "UUID секрета")
        UUID secretUuid,
        @Schema(description = "Название секрета")
        String name,
        @Schema(description = "Область видимости")
        String scope,
        @Schema(description = "Описание")
        String description,
        @Schema(description = "Текущая версия секрета")
        int currentVersion,
        @Schema(description = "Дата создания")
        Instant createdAt,
        @Schema(description = "Дата обновления")
        Instant updatedAt
) {}