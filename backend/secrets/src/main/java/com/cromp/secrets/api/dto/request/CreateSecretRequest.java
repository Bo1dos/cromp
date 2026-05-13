package com.cromp.secrets.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.cromp.secrets.domain.model.enums.SecretScope;

public record CreateSecretRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank String value,           // значение приходит открытым, шифруется в сервисе
        SecretScope scope,
        @Size(max = 1024) String description
) {}