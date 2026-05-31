package com.cromp.iam.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ответ аутентификации: JWT токены, данные пользователя и организации")
public record AuthResponse(
        @Schema(description = "Тип токена (Bearer)", example = "Bearer")
        String tokenType,
        @Schema(description = "JWT access token")
        String accessToken,
        @Schema(description = "Профиль аутентифицированного пользователя")
        UserResponse user,
        @Schema(description = "Список доступных организаций (может быть пустым)")
        List<OrganizationResponse> organizations,
        @Schema(description = "Активная организация (null при логине, заполняется при выборе)", nullable = true)
        OrganizationResponse activeOrganization
) { }