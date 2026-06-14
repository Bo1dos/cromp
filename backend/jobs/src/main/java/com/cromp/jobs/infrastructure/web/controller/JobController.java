package com.cromp.jobs.infrastructure.web.controller;

import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.dto.response.*;
import com.cromp.jobs.api.service.JobFacade;
import com.cromp.jobs.api.service.JobVersionFacade;
import com.cromp.jobs.domain.model.enums.JobStatus;
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

@Tag(name = "Jobs", description = "Управление задачами (cron jobs): CRUD, статусы, версионирование, ручной запуск")
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobFacade jobFacade;
    private final JobVersionFacade jobVersionFacade;

    // ---- CRUD ----

    @Operation(summary = "Создать задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Задача создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:create"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse create(@Valid @RequestBody CreateJobRequest request) {
        return jobFacade.createJob(request);
    }

    @Operation(summary = "Получить список задач")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<JobResponse> list(
            @Parameter(description = "Фильтр по статусу задачи") @RequestParam(name = "status", required = false) JobStatus status,
            @Parameter(description = "Лимит результатов (по умолчанию 20)") @RequestParam(name = "limit", defaultValue = "20") int limit,
            @Parameter(description = "Смещение (по умолчанию 0)") @RequestParam(name = "offset", defaultValue = "0") int offset) {
        return jobFacade.listJobs(status, limit, offset);
    }

    @Operation(summary = "Получить задачу по числовому ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные задачи"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/by-id/{id}")
    @PreAuthorize("isAuthenticated()")
    public JobResponse getById(
            @Parameter(description = "Числовой ID задачи") @PathVariable("id") Long id) {
        return jobFacade.getJobById(id);
    }

    @Operation(summary = "Получить задачу по UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные задачи"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{jobUuid}")
    @PreAuthorize("isAuthenticated()")
    public JobResponse get(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid) {
        return jobFacade.getJob(jobUuid);
    }

    @Operation(summary = "Обновить задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача обновлена"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:update"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PutMapping("/{jobUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:update')")
    public JobResponse update(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid,
            @Valid @RequestBody UpdateJobRequest request) {
        return jobFacade.updateJob(jobUuid, request);
    }

    @Operation(summary = "Удалить задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Задача удалена"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:delete"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{jobUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid) {
        jobFacade.deleteJob(jobUuid);
    }

    @Operation(summary = "Изменить статус задачи (активна/приостановлена)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус изменён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:update"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "409", description = "Недопустимый переход статуса"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PatchMapping("/{jobUuid}/status")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:update')")
    public JobResponse changeStatus(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid,
            @Valid @RequestBody ChangeJobStatusRequest request) {
        return jobFacade.changeStatus(jobUuid, request);
    }

    // ---- Manual Trigger ----

    @Operation(summary = "Запустить задачу вручную")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Задача поставлена в очередь на выполнение"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:execute"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "409", description = "Задача не в статусе, допускающем запуск"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping("/{jobUuid}/trigger")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:execute')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TriggerResponse trigger(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid,
            @Valid @RequestBody TriggerJobRequest request) {
        return jobFacade.triggerJob(jobUuid, request);
    }

    // ---- Versioning ----

    @Operation(summary = "Получить все версии задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список версий"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{jobUuid}/versions")
    @PreAuthorize("isAuthenticated()")
    public List<JobVersionResponse> getVersions(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid) {
        return jobVersionFacade.getVersions(jobUuid);
    }

    @Operation(summary = "Получить конкретную версию задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные версии"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача или версия не найдена")
    })
    @GetMapping("/{jobUuid}/versions/{version}")
    @PreAuthorize("isAuthenticated()")
    public JobVersionResponse getVersion(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid,
            @Parameter(description = "Номер версии") @PathVariable("version") int version) {
        return jobVersionFacade.getVersion(jobUuid, version);
    }

    @Operation(summary = "Сравнить две версии задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Результат сравнения версий"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача или одна из версий не найдена")
    })
    @GetMapping("/{jobUuid}/versions/compare")
    @PreAuthorize("isAuthenticated()")
    public JobVersionComparisonResponse compareVersions(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid,
            @Parameter(description = "Версия «от»") @RequestParam(name = "fromVersion") int fromVersion,
            @Parameter(description = "Версия «до»") @RequestParam(name = "toVersion") int toVersion) {
        return jobVersionFacade.compareVersions(jobUuid, fromVersion, toVersion);
    }

    @Operation(summary = "Откатить задачу к предыдущей версии")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача откачена к указанной версии"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:update"),
            @ApiResponse(responseCode = "404", description = "Задача или версия не найдена"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping("/{jobUuid}/revert")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:update')")
    public JobVersionResponse revert(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid,
            @Valid @RequestBody RevertJobRequest request) {
        return jobVersionFacade.revertToVersion(jobUuid, request.version());
    }

    @Operation(summary = "Получить текущую версию задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Текущая версия задачи"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{jobUuid}/current-version")
    @PreAuthorize("isAuthenticated()")
    public JobVersionResponse currentVersion(
            @Parameter(description = "UUID задачи") @PathVariable("jobUuid") UUID jobUuid) {
        return jobVersionFacade.getCurrentVersion(jobUuid);
    }
}
