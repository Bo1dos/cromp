package com.cromp.secrets.api.service;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;

import java.util.List;
import java.util.UUID;

public interface SecretFacade {
    SecretResponse createSecret(Long organizationId, CreateSecretRequest request);
    SecretResponse getSecret(Long organizationId, UUID secretUuid);
    List<SecretResponse> listSecrets(Long organizationId);
    void deleteSecret(Long organizationId, UUID secretUuid);
    SecretResponse rotateSecret(Long organizationId, UUID secretUuid, RotateSecretRequest request);
    List<SecretVersionResponse> getVersions(Long organizationId, UUID secretUuid);
}