package com.cromp.executions.infrastructure.web.controller;

import com.cromp.executions.api.dto.ExecutionFilter;
import com.cromp.executions.api.dto.request.UploadArtifactRequest;
import com.cromp.executions.api.dto.response.*;
import com.cromp.executions.application.service.ArtifactService;
import com.cromp.executions.application.service.AttemptService;
import com.cromp.executions.application.service.ExecutionService;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.iam.application.port.OrganizationLookupPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Tag(name = "Executions", description = "История запусков задач, попытки выполнения и артефакты")
@RestController
@RequestMapping("/api/v1/organizations/{orgUuid}/executions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ExecutionController {

    private final ExecutionService executionService;
    private final AttemptService attemptService;
    private final ArtifactService artifactService;
    private final OrganizationLookupPort organizationLookupPort;

    /** Резолвит UUID организации во внутренний Long ID, либо 404. */
    private long resolveOrg(UUID orgUuid) {
        return organizationLookupPort.resolveOrganizationId(orgUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found: " + orgUuid));
    }

    @Operation(summary = "Получить список запусков с фильтрацией и пагинацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница с запусками"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ExecutionResponse> list(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Parameter(description = "Фильтр по ID задачи") @RequestParam(name = "jobId", required = false) Long jobId,
            @Parameter(description = "Фильтр по статусу") @RequestParam(name = "status", required = false) ExecutionStatus status,
            @Parameter(description = "Фильтр по источнику запуска") @RequestParam(name = "source", required = false) ExecutionSource source,
            @Parameter(description = "Начало временного диапазона") @RequestParam(name = "from", required = false) Instant from,
            @Parameter(description = "Конец временного диапазона") @RequestParam(name = "to", required = false) Instant to,
            @Parameter(description = "Номер страницы (с 0)") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        long organizationId = resolveOrg(orgUuid);
        ExecutionFilter filter = new ExecutionFilter(jobId, status, source, from, to, page, size);
        return executionService.listExecutions(organizationId, filter);
    }

    @Operation(summary = "Получить детали запуска")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Детали запуска с payload и snapshot"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Организация или запуск не найдены")
    })
    @GetMapping("/{executionId}")
    @PreAuthorize("isAuthenticated()")
    public ExecutionDetailResponse get(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Parameter(description = "UUID запуска") @PathVariable("executionId") UUID executionId
    ) {
        long organizationId = resolveOrg(orgUuid);
        return executionService.getExecution(organizationId, executionId);
    }

    @Operation(summary = "Отменить запуск")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запуск отменён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:execute"),
            @ApiResponse(responseCode = "404", description = "Организация или запуск не найдены"),
            @ApiResponse(responseCode = "409", description = "Запуск уже завершён и не может быть отменён")
    })
    @PostMapping("/{executionId}/cancel")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:execute')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Parameter(description = "UUID запуска") @PathVariable("executionId") UUID executionId
    ) {
        long organizationId = resolveOrg(orgUuid);
        executionService.cancelExecution(organizationId, executionId);
    }

    @Operation(summary = "Получить попытки выполнения запуска")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список попыток"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Организация или запуск не найдены")
    })
    @GetMapping("/{executionId}/attempts")
    @PreAuthorize("isAuthenticated()")
    public List<AttemptResponse> listAttempts(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Parameter(description = "UUID запуска") @PathVariable("executionId") UUID executionId
    ) {
        long organizationId = resolveOrg(orgUuid);
        return attemptService.listAttempts(organizationId, executionId);
    }

    @Operation(summary = "Получить артефакты запуска")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список артефактов"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Организация или запуск не найдены")
    })
    @GetMapping("/{executionId}/artifacts")
    @PreAuthorize("isAuthenticated()")
    public List<ArtifactResponse> listArtifacts(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Parameter(description = "UUID запуска") @PathVariable("executionId") UUID executionId
    ) {
        long organizationId = resolveOrg(orgUuid);
        return artifactService.listArtifacts(organizationId, executionId);
    }

    @Operation(summary = "Загрузить артефакт запуска")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Артефакт загружен"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:execute"),
            @ApiResponse(responseCode = "404", description = "Организация или запуск не найдены"),
            @ApiResponse(responseCode = "422", description = "Некорректные метаданные артефакта")
    })
    @PostMapping(value = "/{executionId}/artifacts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:execute')")
    @ResponseStatus(HttpStatus.CREATED)
    public ArtifactResponse uploadArtifact(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Parameter(description = "UUID запуска") @PathVariable("executionId") UUID executionId,
            @Parameter(description = "Метаданные артефакта (JSON)") @RequestPart("meta") UploadArtifactRequest request,
            @Parameter(description = "Файл артефакта") @RequestPart("file") MultipartFile file
    ) {
        long organizationId = resolveOrg(orgUuid);
        return artifactService.uploadArtifact(organizationId, executionId, request, file);
    }
}
