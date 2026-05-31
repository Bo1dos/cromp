package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.CreateOrganizationRequest;
import com.cromp.iam.api.dto.request.RenameOrganizationRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.dto.response.OrganizationResponse;
import com.cromp.iam.api.service.OrganizationFacade;
import com.cromp.iam.application.port.CurrentActorPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Organizations", description = "Управление организациями")
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationFacade organizationFacade;
    private final CurrentActorPort currentActorPort;

    @Operation(summary = "Создать организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Организация создана"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public OrganizationResponse create(@Valid @RequestBody CreateOrganizationRequest request) {
        Long userId = currentActorPort.currentUserId().orElseThrow();
        return organizationFacade.create(request, userId);
    }

    @Operation(summary = "Переименовать организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Организация переименована"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на переименование"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PutMapping("/{orgUuid}/rename")
    @PreAuthorize("isAuthenticated()")
    public OrganizationResponse rename(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid,
            @Valid @RequestBody RenameOrganizationRequest request) {
        return organizationFacade.rename(orgUuid, request);
    }

    @Operation(summary = "Получить организацию по UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные организации"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @GetMapping("/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public OrganizationResponse get(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid) {
        return organizationFacade.getById(orgUuid);
    }

    @Operation(summary = "Получить список участников организации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список участников"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @GetMapping("/{orgUuid}/members")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getMembers(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid) {
        return organizationFacade.getMembers(orgUuid);
    }
}
