package com.cromp.iam.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record RegisterRequest(
        @Email @NotBlank
        @Schema(description = "Email", example = "user@example.com")
        String email,
        @NotBlank @Size(min = 8, max = 255)
        @Schema(description = "Пароль (мин. 8 символов)", example = "securePassword123")
        String password,
        @NotBlank @Size(max = 255)
        @Schema(description = "Название организации", example = "My Company")
        String organizationName,
        @Size(max = 150)
        @Schema(description = "Имя", example = "Иван")
        String firstName,
        @Size(max = 150)
        @Schema(description = "Фамилия", example = "Петров")
        String lastName,
        @Size(max = 150)
        @Schema(description = "Отчество", example = "Сергеевич")
        String middleName,
        @Size(max = 255)
        @Schema(description = "Отображаемое имя", example = "Иван Петров")
        String displayName
) {}