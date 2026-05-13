package com.cromp.secrets.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.mapper.SecretApiMapper;
import com.cromp.secrets.api.service.SecretFacade;
import com.cromp.secrets.application.port.AuditPort;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.exceptions.InvalidSecretStateException;
import com.cromp.secrets.domain.model.exceptions.SecretNotFoundException;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import com.cromp.secrets.domain.repository.SecretVersionRepositoryPort;
import com.cromp.secrets.domain.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SecretApplicationService implements SecretFacade {

    private final SecretRepositoryPort secretRepository;
    private final SecretVersionRepositoryPort versionRepository;
    private final EncryptionService encryptionService;
    private final SecretApiMapper mapper;
    private final AuditPort auditPort;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    @Override
    public SecretResponse createSecret(Long organizationId, CreateSecretRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "secret:create")) {
            throw new SecurityException("No permission to create secret");
        }
        if (secretRepository.findByNameAndOrganizationId(request.name(), organizationId).isPresent()) {
            throw new InvalidSecretStateException("Secret with this name already exists in organization");
        }
        Secret secret = Secret.create(organizationId, request.name(),
                request.scope() != null ? request.scope() : SecretScope.JOB,
                request.description());
        secret = secretRepository.save(secret);

        byte[] encrypted = encryptionService.encrypt(request.value().getBytes(StandardCharsets.UTF_8));
        SecretVersion version = SecretVersion.createNew(secret.getId(), 1, encrypted, null, userId);
        versionRepository.save(version);

        auditPort.record("SECRET.CREATE", organizationId, userId, "secrets",
                secret.getId(), Map.of("name", request.name()));

        return mapper.toSecretResponse(secret, version);
    }

    @Override
    @Transactional(readOnly = true)
    public SecretResponse getSecret(Long organizationId, UUID secretUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }
        SecretVersion activeVersion = versionRepository.findActiveVersion(secret.getId()).orElse(null);
        return mapper.toSecretResponse(secret, activeVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretResponse> listSecrets(Long organizationId) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        return secretRepository.findByOrganizationId(organizationId).stream()
                .map(s -> {
                    SecretVersion active = versionRepository.findActiveVersion(s.getId()).orElse(null);
                    return mapper.toSecretResponse(s, active);
                })
                .toList();
    }

    @Override
    public void deleteSecret(Long organizationId, UUID secretUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "secret:delete")) {
            throw new SecurityException("No permission to delete secret");
        }
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }
        secret.markDeleted();
        secretRepository.save(secret);
        auditPort.record("SECRET.DELETE", organizationId, userId, "secrets",
                secret.getId(), Map.of("secretUuid", secretUuid.toString()));
    }

    @Override
    public SecretResponse rotateSecret(Long organizationId, UUID secretUuid, RotateSecretRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "secret:update")) {
            throw new SecurityException("No permission to rotate secret");
        }
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }
        // Деактивировать текущую активную версию
        versionRepository.findActiveVersion(secret.getId()).ifPresent(v -> {
            v.deprecate();
            versionRepository.save(v);
        });
        byte[] encrypted = encryptionService.encrypt(request.value().getBytes(StandardCharsets.UTF_8));
        int newVersionNumber = versionRepository.getMaxVersion(secret.getId()).orElse(0) + 1;
        SecretVersion newVersion = SecretVersion.createNew(secret.getId(), newVersionNumber, encrypted, null, userId);
        versionRepository.save(newVersion);
        secret.updateTimestamp();
        secretRepository.save(secret);
        auditPort.record("SECRET.ROTATE", organizationId, userId, "secrets",
                secret.getId(), Map.of("newVersion", newVersionNumber));
        return mapper.toSecretResponse(secret, newVersion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretVersionResponse> getVersions(Long organizationId, UUID secretUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }
        return versionRepository.findBySecretIdOrderByVersionDesc(secret.getId()).stream()
                .map(mapper::toVersionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] resolveSecretForExecution(Long organizationId, UUID secretUuid) {
        // TODO: Пока что нужна защита на уровне контроллера
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }
        SecretVersion activeVersion = versionRepository.findActiveVersion(secret.getId())
                .orElseThrow(() -> new InvalidSecretStateException("No active version for secret"));
        return encryptionService.decrypt(activeVersion.getValueCipher());
    }
}