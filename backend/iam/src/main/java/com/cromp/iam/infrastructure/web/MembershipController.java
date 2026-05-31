package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AddMembershipRequest;
import com.cromp.iam.api.dto.request.ChangeMembershipRoleRequest;
import com.cromp.iam.api.dto.response.MembershipResponse;
import com.cromp.iam.api.service.MembershipFacade;
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

// TODO: Long убрать нахер 
@Tag(name = "Memberships", description = "Управление членством пользователей в организациях")
@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MembershipController {

    private final MembershipFacade membershipFacade;

    @Operation(summary = "Добавить пользователя в организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Членство создано"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на добавление"),
            @ApiResponse(responseCode = "404", description = "Пользователь или организация не найдены"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже состоит в организации"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse add(@Valid @RequestBody AddMembershipRequest request) {
        return membershipFacade.add(request);
    }

    @Operation(summary = "Изменить роль участника")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Роль изменена"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение роли"),
            @ApiResponse(responseCode = "404", description = "Членство не найдено"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PutMapping("/{membershipUuid}/role")
    @PreAuthorize("isAuthenticated()")
    public MembershipResponse changeRole(
            @Parameter(description = "UUID членства") @PathVariable UUID membershipUuid,
            @Valid @RequestBody ChangeMembershipRoleRequest request) {
        return membershipFacade.changeRole(membershipUuid, request);
    }

    @Operation(summary = "Удалить участника из организации")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Участник удалён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление"),
            @ApiResponse(responseCode = "404", description = "Членство не найдено")
    })
    @DeleteMapping("/{membershipUuid}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @Parameter(description = "UUID членства") @PathVariable UUID membershipUuid) {
        membershipFacade.remove(membershipUuid);
    }

    @Operation(summary = "Получить членство по UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные членства"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Членство не найдено")
    })
    @GetMapping("/{membershipUuid}")
    @PreAuthorize("isAuthenticated()")
    public MembershipResponse getById(
            @Parameter(description = "UUID членства") @PathVariable UUID membershipUuid) {
        return membershipFacade.getById(membershipUuid);
    }

    @Operation(summary = "Получить всех участников организации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список участников"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    })
    @GetMapping("/organization/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getByOrganization(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid) {
        return membershipFacade.getByOrganization(orgUuid);
    }

    @Operation(summary = "Получить все членства пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список членств"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    })
    @GetMapping("/user/{userUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<MembershipResponse> getByUser(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userUuid) {
        return membershipFacade.getByUser(userUuid);
    }
}
