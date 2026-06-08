package com.cromp.notifications.infrastructure.web.controller;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.notifications.api.dto.response.PagedNotificationResponse;
import com.cromp.notifications.api.dto.response.UnreadCountResponse;
import com.cromp.notifications.api.service.NotificationFacade;
import com.cromp.notifications.domain.model.NotificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Notifications", description = "Уведомления пользователя")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationFacade notificationFacade;
    private final CurrentActorPort currentActorPort;

    @Operation(summary = "Получить список уведомлений")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedNotificationResponse list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        NotificationStatus ns = status != null ? NotificationStatus.valueOf(status.toUpperCase()) : null;
        return notificationFacade.list(getCurrentUserId(), ns, page, size);
    }

    @Operation(summary = "Количество непрочитанных уведомлений")
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public UnreadCountResponse unreadCount() {
        return notificationFacade.unreadCount(getCurrentUserId());
    }

    @Operation(summary = "Отметить уведомление прочитанным")
    @PostMapping("/{notificationUuid}/mark-read")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable UUID notificationUuid) {
        notificationFacade.markRead(getCurrentUserId(), notificationUuid);
    }

    @Operation(summary = "Отметить все уведомления прочитанными")
    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead() {
        notificationFacade.markAllRead(getCurrentUserId());
    }

    @Operation(summary = "Архивировать уведомление")
    @DeleteMapping("/{notificationUuid}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID notificationUuid) {
        notificationFacade.delete(getCurrentUserId(), notificationUuid);
    }

    private Long getCurrentUserId() {
        return currentActorPort.currentUserId()
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));
    }
}
