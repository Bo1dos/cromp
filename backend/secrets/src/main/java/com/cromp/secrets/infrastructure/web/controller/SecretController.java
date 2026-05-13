package com.cromp.secrets.infrastructure.web.controller;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.service.SecretFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/secrets")
@RequiredArgsConstructor
public class SecretController {

    private final SecretFacade secretFacade;

    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecretResponse create(@PathVariable Long organizationId,
                                 @Valid @RequestBody CreateSecretRequest request) {
        return secretFacade.createSecret(organizationId, request);
    }

    @GetMapping
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<SecretResponse> list(@PathVariable Long organizationId) {
        return secretFacade.listSecrets(organizationId);
    }

    @GetMapping("/{secretUuid}")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public SecretResponse get(@PathVariable Long organizationId,
                              @PathVariable UUID secretUuid) {
        return secretFacade.getSecret(organizationId, secretUuid);
    }

    @DeleteMapping("/{secretUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long organizationId,
                       @PathVariable UUID secretUuid) {
        secretFacade.deleteSecret(organizationId, secretUuid);
    }

    @PostMapping("/{secretUuid}/rotate")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:update')")
    public SecretResponse rotate(@PathVariable Long organizationId,
                                 @PathVariable UUID secretUuid,
                                 @Valid @RequestBody RotateSecretRequest request) {
        return secretFacade.rotateSecret(organizationId, secretUuid, request);
    }

    @GetMapping("/{secretUuid}/versions")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<SecretVersionResponse> getVersions(@PathVariable Long organizationId,
                                                   @PathVariable UUID secretUuid) {
        return secretFacade.getVersions(organizationId, secretUuid);
    }
}