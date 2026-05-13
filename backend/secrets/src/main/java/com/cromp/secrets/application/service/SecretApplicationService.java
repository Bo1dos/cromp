package com.cromp.secrets.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.request.UpdateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretValueResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.mapper.SecretApiMapper;
import com.cromp.secrets.api.service.SecretFacade;
import com.cromp.secrets.application.port.SecretCipherPort;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.exceptions.SecretNotFoundException;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import com.cromp.secrets.domain.repository.SecretVersionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SecretApplicationService implements SecretFacade {
    private final SecretRepositoryPort secretRepository;
    private final SecretVersionRepositoryPort versionRepository;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final SecretCipherPort cipherPort;
    private final SecretApiMapper mapper;

    @Override
    public SecretResponse createSecret(Long organizationId, CreateSecretRequest request) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "secret:create");
        if (secretRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
            throw new IllegalStateException("Secret with this name already exists");
        }
        Secret secret = secretRepository.save(Secret.create(UUID.randomUUID(), organizationId, request.name(),
                SecretScope.valueOf(request.scope()), request.description()));
        SecretCipherPort.EncryptedSecret encrypted = cipherPort.encrypt(request.value());
        SecretVersion version = versionRepository.save(SecretVersion.create(secret.getId(), 1,
                encrypted.cipherText(), encrypted.keyId(), userId));
        return mapper.toResponse(secret, version);
    }

    @Override
    public SecretResponse updateSecret(Long organizationId, Long secretId, UpdateSecretRequest request) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "secret:update");
        Secret secret = requireSecret(organizationId, secretId);
        if (request.name() != null && !request.name().equals(secret.getName())) {
            if (secretRepository.existsByNameAndOrganizationId(request.name(), organizationId)) {
                throw new IllegalStateException("Secret with this name already exists");
            }
            secret.rename(request.name());
        }
        if (request.description() != null) secret.changeDescription(request.description());
        if (request.scope() != null) secret.changeScope(SecretScope.valueOf(request.scope()));
        secret = secretRepository.save(secret);
        return mapper.toResponse(secret, versionRepository.findActiveBySecretId(secretId).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public SecretResponse getSecret(Long organizationId, Long secretId) {
        requireMember(requireUser(), organizationId);
        Secret secret = requireSecret(organizationId, secretId);
        return mapper.toResponse(secret, versionRepository.findActiveBySecretId(secretId).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretResponse> listSecrets(Long organizationId) {
        requireMember(requireUser(), organizationId);
        return secretRepository.findByOrganizationId(organizationId).stream()
                .map(secret -> mapper.toResponse(secret, versionRepository.findActiveBySecretId(secret.getId()).orElse(null)))
                .toList();
    }

    @Override
    public SecretVersionResponse rotateSecret(Long organizationId, Long secretId, RotateSecretRequest request) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "secret:update");
        requireSecret(organizationId, secretId);
        int nextVersion = versionRepository.findBySecretId(secretId).stream()
                .mapToInt(SecretVersion::getVersion)
                .max()
                .orElse(0) + 1;
        versionRepository.deactivateActiveVersions(secretId);
        SecretCipherPort.EncryptedSecret encrypted = cipherPort.encrypt(request.value());
        SecretVersion version = versionRepository.save(SecretVersion.create(secretId, nextVersion,
                encrypted.cipherText(), encrypted.keyId(), userId));
        return mapper.toVersionResponse(version);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretVersionResponse> listVersions(Long organizationId, Long secretId) {
        requireMember(requireUser(), organizationId);
        requireSecret(organizationId, secretId);
        return versionRepository.findBySecretId(secretId).stream().map(mapper::toVersionResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SecretValueResponse revealSecret(Long organizationId, Long secretId, Integer version) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "secret:read");
        requireSecret(organizationId, secretId);
        SecretVersion secretVersion = version == null
                ? versionRepository.findActiveBySecretId(secretId).orElseThrow(() -> new SecretNotFoundException(secretId))
                : versionRepository.findBySecretIdAndVersion(secretId, version).orElseThrow(() -> new SecretNotFoundException(secretId));
        return new SecretValueResponse(secretId, secretVersion.getVersion(), cipherPort.decrypt(secretVersion.getValueCipher()));
    }

    @Override
    public void deleteSecret(Long organizationId, Long secretId) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "secret:delete");
        Secret secret = requireSecret(organizationId, secretId);
        secret.delete();
        secretRepository.save(secret);
    }

    private Secret requireSecret(Long organizationId, Long secretId) {
        return secretRepository.findByIdAndOrganizationId(secretId, organizationId)
                .orElseThrow(() -> new SecretNotFoundException(secretId));
    }

    private Long requireUser() {
        return currentActorPort.currentUserId().orElseThrow(() -> new SecurityException("Not authenticated"));
    }

    private void requireMember(Long userId, Long organizationId) {
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
    }

    private void requirePermission(Long userId, Long organizationId, String permission) {
        if (!permissionCheckerPort.hasPermission(userId, organizationId, permission)) {
            throw new SecurityException("No permission: " + permission);
        }
    }
}
