package com.cromp.secrets.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cromp.secrets.domain.model.enums.SecretScope;

@Schema(description = "Запрос на создание секрета. Значение передаётся открытым текстом и шифруется на сервере.")
public record CreateSecretRequest(
        @NotBlank @Size(max = 255)
        @Schema(description = "Название секрета", example = "DB_PASSWORD")
        String name,
        @NotBlank
        @Schema(description = "Значение секрета (передаётся открытым текстом, НЕ возвращается в ответах)",
                example = "s3cr3tV@lue")
        String value,
        @Schema(description = "Область видимости секрета", example = "ORGANIZATION")
        SecretScope scope,
        @Size(max = 1024)
        @Schema(description = "Описание секрета", example = "Пароль к production БД")
        String description
) {}