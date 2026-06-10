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
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;

    @Override
    public SecretResponse createSecret(CreateSecretRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
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
    public SecretResponse getSecret(UUID secretUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
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
    public List<SecretResponse> listSecrets() {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
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
    public void deleteSecret(UUID secretUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "secret:delete")) {
            throw new SecurityException("No permission to delete secret");
        }
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }

        // Проверяем, не используется ли секрет в активных job-ах
        checkSecretReferences(secretUuid, organizationId);

        secret.markDeleted();
        secretRepository.save(secret);
        auditPort.record("SECRET.DELETE", organizationId, userId, "secrets",
                secret.getId(), Map.of("secretUuid", secretUuid.toString()));
    }

    @Override
    public SecretResponse rotateSecret(UUID secretUuid, RotateSecretRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "secret:update")) {
            throw new SecurityException("No permission to rotate secret");
        }
        Secret secret = secretRepository.findBySecretUuid(secretUuid)
                .orElseThrow(() -> new SecretNotFoundException("secretUuid=" + secretUuid));
        if (!secret.getOrganizationId().equals(organizationId)) {
            throw new SecurityException("Secret does not belong to organization");
        }
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
    public List<SecretVersionResponse> getVersions(UUID secretUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
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

    /**
     * Проверяет, что секрет не используется ни в одной активной Job.
     * Ищет по JSONB-полю config в job_versions (последняя версия не-удалённой job).
     */
    private void checkSecretReferences(UUID secretUuid, Long organizationId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM job_versions jv
                JOIN jobs j ON j.id = jv.job_id
                WHERE j.deleted_at IS NULL
                  AND j.organization_id = ?
                  AND jv.config::text LIKE ?
                """,
                Integer.class,
                organizationId,
                "%" + secretUuid + "%"
        );
        if (count != null && count > 0) {
            throw new InvalidSecretStateException(
                    "Secret is still referenced by " + count + " active job(s). Remove references first.");
        }
    }
}