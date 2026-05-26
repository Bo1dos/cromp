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
@RequestMapping("/api/v1/secrets")
@RequiredArgsConstructor
public class SecretController {

    private final SecretFacade secretFacade;

    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'secret:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecretResponse create(@Valid @RequestBody CreateSecretRequest request) {
        return secretFacade.createSecret(request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SecretResponse> list() {
        return secretFacade.listSecrets();
    }

    @GetMapping("/{secretUuid}")
    @PreAuthorize("isAuthenticated()")
    public SecretResponse get(@PathVariable UUID secretUuid) {
        return secretFacade.getSecret(secretUuid);
    }

    @DeleteMapping("/{secretUuid}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'secret:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID secretUuid) {
        secretFacade.deleteSecret(secretUuid);
    }

    @PostMapping("/{secretUuid}/rotate")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'secret:update')")
    public SecretResponse rotate(@PathVariable UUID secretUuid,
                                 @Valid @RequestBody RotateSecretRequest request) {
        return secretFacade.rotateSecret(secretUuid, request);
    }

    @GetMapping("/{secretUuid}/versions")
    @PreAuthorize("isAuthenticated()")
    public List<SecretVersionResponse> getVersions(@PathVariable UUID secretUuid) {
        return secretFacade.getVersions(secretUuid);
    }
}