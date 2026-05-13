package com.cromp.secrets.api.service;

import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.request.UpdateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretValueResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;

import java.util.List;

public interface SecretFacade {
    SecretResponse createSecret(Long organizationId, CreateSecretRequest request);
    SecretResponse updateSecret(Long organizationId, Long secretId, UpdateSecretRequest request);
    SecretResponse getSecret(Long organizationId, Long secretId);
    List<SecretResponse> listSecrets(Long organizationId);
    SecretVersionResponse rotateSecret(Long organizationId, Long secretId, RotateSecretRequest request);
    List<SecretVersionResponse> listVersions(Long organizationId, Long secretId);
    SecretValueResponse revealSecret(Long organizationId, Long secretId, Integer version);
    void deleteSecret(Long organizationId, Long secretId);
}
