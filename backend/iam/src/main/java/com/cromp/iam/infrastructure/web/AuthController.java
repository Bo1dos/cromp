package com.cromp.iam.infrastructure.web;

import com.cromp.iam.api.dto.request.LoginRequest;
import com.cromp.iam.api.dto.request.RegisterRequest;
import com.cromp.iam.api.dto.request.SelectOrganizationRequest;
import com.cromp.iam.api.dto.response.AuthResponse;
import com.cromp.iam.application.service.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Регистрация, аутентификация и выбор организации")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт нового пользователя вместе с его первой организацией. Возвращает JWT токены."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя по email и паролю, возвращает JWT токены."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные"),
            @ApiResponse(responseCode = "422", description = "Некорректные данные запроса")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(
            summary = "Выбор активной организации",
            description = "Устанавливает выбранную организацию в качестве активной для текущей сессии. Требует аутентификации."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Организация выбрана"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Пользователь не принадлежит организации"),
            @ApiResponse(responseCode = "404", description = "Организация не найдена")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/select-organization")
    public AuthResponse selectOrganization(@Valid @RequestBody SelectOrganizationRequest request) {
        return authService.selectOrganization(request);
    }
}