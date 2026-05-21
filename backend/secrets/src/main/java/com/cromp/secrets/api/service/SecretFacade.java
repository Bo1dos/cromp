package com.cromp.secrets.api.service;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;

import java.util.List;
import java.util.UUID;

public interface SecretFacade {
    SecretResponse createSecret(CreateSecretRequest request);
    SecretResponse getSecret(UUID secretUuid);
    List<SecretResponse> listSecrets();
    void deleteSecret(UUID secretUuid);
    SecretResponse rotateSecret(UUID secretUuid, RotateSecretRequest request);
    List<SecretVersionResponse> getVersions(UUID secretUuid);
}