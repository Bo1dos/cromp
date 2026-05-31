package com.cromp.iam.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на вход в систему")
public record LoginRequest(
        @Email @NotBlank
        @Schema(description = "Email пользователя", example = "user@example.com")
        String email,
        @NotBlank
        @Schema(description = "Пароль", example = "securePassword123")
        String password
) {}