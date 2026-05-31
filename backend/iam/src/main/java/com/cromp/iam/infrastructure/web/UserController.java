package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.ChangeUserEmailRequest;
import com.cromp.iam.api.dto.request.ChangeUserPasswordRequest;
import com.cromp.iam.api.dto.request.UpdateUserProfileRequest;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.api.service.UserFacade;
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

@Tag(name = "Users", description = "Управление профилями пользователей")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserFacade userFacade;

    @Operation(summary = "Получить профиль пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к профилю"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{userUuid}")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getUser(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userUuid) {
        return userFacade.getById(userUuid);
    }

    @Operation(summary = "Обновить профиль пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль обновлён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение профиля"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PutMapping("/{userUuid}/profile")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateProfile(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userUuid,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return userFacade.updateProfile(userUuid, request);
    }

    @Operation(summary = "Изменить email пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email изменён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение email"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "422", description = "Некорректный email")
    })
    @PutMapping("/{userUuid}/email")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userUuid,
            @Valid @RequestBody ChangeUserEmailRequest request) {
        userFacade.changeEmail(userUuid, request);
    }

    @Operation(summary = "Изменить пароль пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пароль изменён"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован / неверный старый пароль"),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение пароля"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PutMapping("/{userUuid}/password")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @Parameter(description = "UUID пользователя") @PathVariable UUID userUuid,
            @Valid @RequestBody ChangeUserPasswordRequest request) {
        userFacade.changePassword(userUuid, request);
    }
}
