package com.cromp.notifications.infrastructure.web.controller;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.notifications.api.dto.request.UpdateNotificationPreferencesRequest;
import com.cromp.notifications.api.dto.response.NotificationPreferencesResponse;
import com.cromp.notifications.api.service.NotificationPreferencesFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Preferences", description = "Настройки уведомлений")
@RestController
@RequestMapping("/api/v1/notification-preferences")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationPreferencesController {

    private final NotificationPreferencesFacade preferencesFacade;
    private final CurrentActorPort currentActorPort;

    @Operation(summary = "Получить настройки уведомлений")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationPreferencesResponse get() {
        return preferencesFacade.get(getCurrentUserId());
    }

    @Operation(summary = "Обновить настройки уведомлений")
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public NotificationPreferencesResponse update(@RequestBody UpdateNotificationPreferencesRequest request) {
        return preferencesFacade.update(getCurrentUserId(), request);
    }

    private Long getCurrentUserId() {
        return currentActorPort.currentUserId()
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));
    }
}
