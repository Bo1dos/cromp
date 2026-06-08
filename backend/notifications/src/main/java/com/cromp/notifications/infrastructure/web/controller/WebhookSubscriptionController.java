package com.cromp.notifications.infrastructure.web.controller;

import com.cromp.notifications.api.dto.request.CreateWebhookSubscriptionRequest;
import com.cromp.notifications.api.dto.response.WebhookDeliveryResponse;
import com.cromp.notifications.api.dto.response.WebhookSubscriptionResponse;
import com.cromp.notifications.api.service.WebhookSubscriptionFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Webhook Subscriptions", description = "Управление webhook-подписками")
@RestController
@RequestMapping("/api/v1/webhook-subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WebhookSubscriptionController {

    private final WebhookSubscriptionFacade subscriptionFacade;

    @Operation(summary = "Список подписок организации")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<WebhookSubscriptionResponse> list() {
        return subscriptionFacade.listByOrg(getCurrentOrgId());
    }

    @Operation(summary = "Создать подписку")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookSubscriptionResponse create(@RequestBody CreateWebhookSubscriptionRequest request) {
        return subscriptionFacade.create(getCurrentOrgId(), request);
    }

    @Operation(summary = "Обновить подписку")
    @PutMapping("/{subscriptionUuid}")
    @PreAuthorize("isAuthenticated()")
    public WebhookSubscriptionResponse update(
            @PathVariable UUID subscriptionUuid,
            @RequestBody CreateWebhookSubscriptionRequest request) {
        return subscriptionFacade.update(getCurrentOrgId(), subscriptionUuid, request);
    }

    @Operation(summary = "Удалить подписку")
    @DeleteMapping("/{subscriptionUuid}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID subscriptionUuid) {
        subscriptionFacade.delete(getCurrentOrgId(), subscriptionUuid);
    }

    @Operation(summary = "Обновить секрет подписки")
    @PostMapping("/{subscriptionUuid}/rotate-secret")
    @PreAuthorize("isAuthenticated()")
    public WebhookSubscriptionResponse rotateSecret(@PathVariable UUID subscriptionUuid) {
        return subscriptionFacade.rotateSecret(getCurrentOrgId(), subscriptionUuid);
    }

    @Operation(summary = "История доставок")
    @GetMapping("/{subscriptionUuid}/deliveries")
    @PreAuthorize("isAuthenticated()")
    public List<WebhookDeliveryResponse> deliveries(
            @PathVariable UUID subscriptionUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return subscriptionFacade.getDeliveries(getCurrentOrgId(), subscriptionUuid, page, size);
    }

    private Long getCurrentOrgId() {
        return 1L;
    }
}
