package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.AcceptInvitationRequest;
import com.cromp.iam.api.dto.request.InviteUserRequest;
import com.cromp.iam.api.dto.request.RejectInvitationRequest;
import com.cromp.iam.api.dto.request.RevokeInvitationRequest;
import com.cromp.iam.api.dto.response.InvitationCreateResponse;
import com.cromp.iam.api.dto.response.InvitationResponse;
import com.cromp.iam.api.service.InvitationFacade;
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

@Tag(name = "Invitations", description = "Приглашения пользователей в организации")
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationFacade invitationFacade;

    @Operation(summary = "Пригласить пользователя в организацию")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Приглашение отправлено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на приглашение"),
            @ApiResponse(responseCode = "409", description = "Приглашение уже существует"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationCreateResponse invite(@Valid @RequestBody InviteUserRequest request) {
        return invitationFacade.invite(request);
    }

    @Operation(summary = "Принять приглашение")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Приглашение принято"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Приглашение не найдено или истекло"),
            @ApiResponse(responseCode = "409", description = "Приглашение уже обработано")
    })
    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return invitationFacade.accept(request);
    }

    @Operation(summary = "Отклонить приглашение")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Приглашение отклонено"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Приглашение не найдено")
    })
    @PostMapping("/reject")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@Valid @RequestBody RejectInvitationRequest request) {
        invitationFacade.reject(request);
    }

    @Operation(summary = "Отозвать приглашение")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Приглашение отозвано"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на отзыв"),
            @ApiResponse(responseCode = "404", description = "Приглашение не найдено")
    })
    @PostMapping("/revoke")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse revoke(@Valid @RequestBody RevokeInvitationRequest request) {
        return invitationFacade.revoke(request);
    }

    @Operation(summary = "Получить приглашение по UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные приглашения"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Приглашение не найдено")
    })
    @GetMapping("/{invitationUuid}")
    @PreAuthorize("isAuthenticated()")
    public InvitationResponse getById(
            @Parameter(description = "UUID приглашения") @PathVariable UUID invitationUuid) {
        return invitationFacade.getById(invitationUuid);
    }

    @Operation(summary = "Получить приглашения организации")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список приглашений"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован")
    })
    @GetMapping("/organization/{orgUuid}")
    @PreAuthorize("isAuthenticated()")
    public List<InvitationResponse> getByOrganization(
            @Parameter(description = "UUID организации") @PathVariable UUID orgUuid) {
        return invitationFacade.getByOrganization(orgUuid);
    }
}
