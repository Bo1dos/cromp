package com.cromp.secrets.infrastructure.web.controller;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.service.SecretFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Secrets", description = "Управление секретами: создание, чтение, ротация, версионирование")
@RestController
@RequestMapping("/api/v1/secrets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SecretController {

    private final SecretFacade secretFacade;

    @Operation(summary = "Создать секрет",
            description = "Создаёт новый секрет. Значение передаётся открытым текстом и шифруется на стороне сервера. В ответе значение не возвращается.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Секрет создан"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав secret:create"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'secret:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecretResponse create(@Valid @RequestBody CreateSecretRequest request) {
        return secretFacade.createSecret(request);
    }

    @Operation(summary = "Получить список секретов",
            description = "Возвращает список всех секретов организации. Значения секретов не возвращаются.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список секретов"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SecretResponse> list() {
        return secretFacade.listSecrets();
    }

    @Operation(summary = "Получить секрет по UUID",
            description = "Возвращает метаданные секрета. Значение секрета не возвращается.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные секрета"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Секрет не найден")
    })
    @GetMapping("/{secretUuid}")
    @PreAuthorize("isAuthenticated()")
    public SecretResponse get(
            @Parameter(description = "UUID секрета") @PathVariable("secretUuid") UUID secretUuid) {
        return secretFacade.getSecret(secretUuid);
    }

    @Operation(summary = "Удалить секрет")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Секрет удалён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав secret:delete"),
            @ApiResponse(responseCode = "404", description = "Секрет не найден")
    })
    @DeleteMapping("/{secretUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'secret:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "UUID секрета") @PathVariable("secretUuid") UUID secretUuid) {
        secretFacade.deleteSecret(secretUuid);
    }

    @Operation(summary = "Ротировать секрет",
            description = "Создаёт новую версию секрета с новым значением. Предыдущие версии сохраняются.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Секрет обновлён (создана новая версия)"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав secret:update"),
            @ApiResponse(responseCode = "404", description = "Секрет не найден"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping("/{secretUuid}/rotate")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'secret:update')")
    public SecretResponse rotate(
            @Parameter(description = "UUID секрета") @PathVariable("secretUuid") UUID secretUuid,
            @Valid @RequestBody RotateSecretRequest request) {
        return secretFacade.rotateSecret(secretUuid, request);
    }

    @Operation(summary = "Получить версии секрета",
            description = "Возвращает список всех версий секрета. Значения не возвращаются.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список версий секрета"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Секрет не найден")
    })
    @GetMapping("/{secretUuid}/versions")
    @PreAuthorize("isAuthenticated()")
    public List<SecretVersionResponse> getVersions(
            @Parameter(description = "UUID секрета") @PathVariable("secretUuid") UUID secretUuid) {
        return secretFacade.getVersions(secretUuid);
    }
}
