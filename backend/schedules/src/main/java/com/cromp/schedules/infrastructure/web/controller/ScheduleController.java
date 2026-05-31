package com.cromp.schedules.infrastructure.web.controller;

import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.api.service.ScheduleFacade;
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

import java.util.UUID;

@Tag(name = "Schedules", description = "Управление cron-расписаниями задач")
@RestController
@RequestMapping("/api/v1/jobs/{jobUuid}/schedule")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {

    private final ScheduleFacade scheduleFacade;

    @Operation(summary = "Создать или обновить расписание задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Расписание создано или обновлено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:manage"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "422", description = "Некорректный cron expression или данные запроса")
    })
    @PutMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:manage')")
    public ScheduleResponse createOrUpdate(
            @Parameter(description = "UUID задачи") @PathVariable UUID jobUuid,
            @Valid @RequestBody CreateUpdateScheduleRequest request) {
        return scheduleFacade.createOrUpdate(jobUuid, request);
    }

    @Operation(summary = "Получить расписание задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Расписание задачи"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Задача или расписание не найдены")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ScheduleResponse get(
            @Parameter(description = "UUID задачи") @PathVariable UUID jobUuid) {
        return scheduleFacade.getSchedule(jobUuid);
    }

    @Operation(summary = "Удалить расписание задачи")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Расписание удалено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав job:manage"),
            @ApiResponse(responseCode = "404", description = "Задача или расписание не найдены")
    })
    @DeleteMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'job:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "UUID задачи") @PathVariable UUID jobUuid) {
        scheduleFacade.deleteSchedule(jobUuid);
    }
}