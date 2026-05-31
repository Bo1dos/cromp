package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.response.AuditLogResponse;
import com.cromp.iam.api.service.AuditLogFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Audit", description = "Журнал аудита действий")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogFacade auditLogFacade;

    @Operation(summary = "Получить аудит-лог организации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список записей аудита"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к аудиту организации")
    })
    @GetMapping("/organization/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<AuditLogResponse> getByOrganization(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid) {
        return auditLogFacade.getByOrganization(orgUuid);
    }

    @Operation(summary = "Получить аудит-лог пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список записей аудита"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к аудиту пользователя")
    })
    @GetMapping("/actor/{userUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<AuditLogResponse> getByActor(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userUuid) {
        return auditLogFacade.getByActor(userUuid);
    }
}
