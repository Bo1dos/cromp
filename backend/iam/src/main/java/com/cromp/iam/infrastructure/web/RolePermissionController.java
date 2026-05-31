package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AddRolePermissionRequest;
import com.cromp.iam.api.dto.response.RolePermissionResponse;
import com.cromp.iam.api.service.RolePermissionFacade;
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

@Tag(name = "Roles & Permissions", description = "Управление ролями и разрешениями")
@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RolePermissionController {

    private final RolePermissionFacade facade;

    @Operation(summary = "Добавить разрешение роли")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Разрешение добавлено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав role:manage"),
            @ApiResponse(responseCode = "404", description = "Роль не найдена"),
            @ApiResponse(responseCode = "409", description = "Разрешение уже существует"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")
    @ResponseStatus(HttpStatus.CREATED)
    public RolePermissionResponse add(@Valid @RequestBody AddRolePermissionRequest request) {
        return facade.add(request);
    }

    @Operation(summary = "Удалить разрешение роли")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Разрешение удалено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав role:manage"),
            @ApiResponse(responseCode = "404", description = "Разрешение не найдено")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @Parameter(description = "ID разрешения") @PathVariable Long id) {
        facade.remove(id);
    }

    @Operation(summary = "Получить разрешения роли")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список разрешений"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав role:manage")
    })
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('role:manage')")
    public List<RolePermissionResponse> getByRoleId(
            @Parameter(description = "ID роли") @PathVariable Long roleId) {
        return facade.getByRoleId(roleId);
    }
}