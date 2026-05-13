package com.cromp.secrets.infrastructure.web.controller;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.request.UpdateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretValueResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.service.SecretFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/secrets")
@RequiredArgsConstructor
public class SecretController {
    private final SecretFacade secretFacade;

    @PostMapping
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:create')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecretResponse create(@PathVariable Long organizationId, @Valid @RequestBody CreateSecretRequest request) {
        return secretFacade.createSecret(organizationId, request);
    }

    @GetMapping
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<SecretResponse> list(@PathVariable Long organizationId) {
        return secretFacade.listSecrets(organizationId);
    }

    @GetMapping("/{secretId}")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public SecretResponse get(@PathVariable Long organizationId, @PathVariable Long secretId) {
        return secretFacade.getSecret(organizationId, secretId);
    }

    @PutMapping("/{secretId}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:update')")
    public SecretResponse update(@PathVariable Long organizationId,
                                 @PathVariable Long secretId,
                                 @Valid @RequestBody UpdateSecretRequest request) {
        return secretFacade.updateSecret(organizationId, secretId, request);
    }

    @DeleteMapping("/{secretId}")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long organizationId, @PathVariable Long secretId) {
        secretFacade.deleteSecret(organizationId, secretId);
    }

    @PostMapping("/{secretId}/versions")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:update')")
    @ResponseStatus(HttpStatus.CREATED)
    public SecretVersionResponse rotate(@PathVariable Long organizationId,
                                        @PathVariable Long secretId,
                                        @Valid @RequestBody RotateSecretRequest request) {
        return secretFacade.rotateSecret(organizationId, secretId, request);
    }

    @GetMapping("/{secretId}/versions")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<SecretVersionResponse> versions(@PathVariable Long organizationId, @PathVariable Long secretId) {
        return secretFacade.listVersions(organizationId, secretId);
    }

    @GetMapping("/{secretId}/value")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'secret:read')")
    public SecretValueResponse reveal(@PathVariable Long organizationId,
                                      @PathVariable Long secretId,
                                      @RequestParam(required = false) Integer version) {
        return secretFacade.revealSecret(organizationId, secretId, version);
    }
}
