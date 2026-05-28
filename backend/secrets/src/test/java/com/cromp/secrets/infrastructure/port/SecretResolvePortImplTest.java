package com.cromp.secrets.infrastructure.port;

import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.exceptions.SecretNotFoundException;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import com.cromp.secrets.domain.repository.SecretVersionRepositoryPort;
import com.cromp.secrets.domain.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretResolvePortImplTest {

    @Mock
    private SecretRepositoryPort secretRepository;
    @Mock
    private SecretVersionRepositoryPort versionRepository;
    @Mock
    private EncryptionService encryptionService;

    private SecretResolvePortImpl port;

    @BeforeEach
    void setUp() {
        port = new SecretResolvePortImpl(secretRepository, versionRepository, encryptionService);
    }

    @Test
    void shouldResolveSecretsInInputOrderAndDecryptEachValue() {
        Secret firstSecret = secret(1L, 100L, "first");
        Secret secondSecret = secret(2L, 100L, "second");
        SecretVersion firstVersion = version(firstSecret.getId(), 1, "alpha");
        SecretVersion secondVersion = version(secondSecret.getId(), 1, "beta");

        when(secretRepository.findBySecretUuid(secondSecret.getSecretUuid())).thenReturn(Optional.of(secondSecret));
        when(secretRepository.findBySecretUuid(firstSecret.getSecretUuid())).thenReturn(Optional.of(firstSecret));
        when(versionRepository.findActiveVersion(secondSecret.getId())).thenReturn(Optional.of(secondVersion));
        when(versionRepository.findActiveVersion(firstSecret.getId())).thenReturn(Optional.of(firstVersion));
        when(encryptionService.decrypt(secondVersion.getValueCipher()))
                .thenReturn("beta".getBytes(StandardCharsets.UTF_8));
        when(encryptionService.decrypt(firstVersion.getValueCipher()))
                .thenReturn("alpha".getBytes(StandardCharsets.UTF_8));

        var resolved = port.resolveSecrets(100L, List.of(secondSecret.getSecretUuid(), firstSecret.getSecretUuid()));

        assertThat(new ArrayList<>(resolved.keySet()))
                .containsExactly(secondSecret.getSecretUuid(), firstSecret.getSecretUuid());
        assertThat(resolved).containsEntry(firstSecret.getSecretUuid(), "alpha");
        assertThat(resolved).containsEntry(secondSecret.getSecretUuid(), "beta");
        verify(encryptionService).decrypt(secondVersion.getValueCipher());
        verify(encryptionService).decrypt(firstVersion.getValueCipher());
    }

    @Test
    void shouldReturnEmptyMapWhenInputListIsEmpty() {
        var resolved = port.resolveSecrets(100L, List.of());

        assertThat(resolved).isEmpty();
        verifyNoInteractions(secretRepository, versionRepository, encryptionService);
    }

    @Test
    void shouldThrowWhenSecretDoesNotExist() {
        UUID secretUuid = UUID.randomUUID();

        when(secretRepository.findBySecretUuid(secretUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> port.resolveSecrets(100L, List.of(secretUuid)))
                .isInstanceOf(SecretNotFoundException.class)
                .hasMessageContaining(secretUuid.toString());
    }

    @Test
    void shouldThrowWhenSecretBelongsToAnotherOrganization() {
        Secret secret = secret(1L, 100L, "first");
        UUID secretUuid = secret.getSecretUuid();

        when(secretRepository.findBySecretUuid(secretUuid)).thenReturn(Optional.of(secret));

        assertThatThrownBy(() -> port.resolveSecrets(200L, List.of(secretUuid)))
                .isInstanceOf(SecretNotFoundException.class)
                .hasMessageContaining(secretUuid.toString());
    }

    @Test
    void shouldThrowWhenActiveVersionIsMissing() {
        Secret secret = secret(1L, 100L, "first");
        UUID secretUuid = secret.getSecretUuid();

        when(secretRepository.findBySecretUuid(secretUuid)).thenReturn(Optional.of(secret));
        when(versionRepository.findActiveVersion(secret.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> port.resolveSecrets(100L, List.of(secretUuid)))
                .isInstanceOf(SecretNotFoundException.class)
                .hasMessageContaining("No active version");
    }

    @Test
    void shouldResolveSeveralSecretsSuccessfully() {
        Secret firstSecret = secret(1L, 100L, "first");
        Secret secondSecret = secret(2L, 100L, "second");
        SecretVersion firstVersion = version(firstSecret.getId(), 1, "alpha");
        SecretVersion secondVersion = version(secondSecret.getId(), 1, "beta");

        when(secretRepository.findBySecretUuid(firstSecret.getSecretUuid())).thenReturn(Optional.of(firstSecret));
        when(secretRepository.findBySecretUuid(secondSecret.getSecretUuid())).thenReturn(Optional.of(secondSecret));
        when(versionRepository.findActiveVersion(firstSecret.getId())).thenReturn(Optional.of(firstVersion));
        when(versionRepository.findActiveVersion(secondSecret.getId())).thenReturn(Optional.of(secondVersion));
        when(encryptionService.decrypt(firstVersion.getValueCipher()))
                .thenReturn("alpha".getBytes(StandardCharsets.UTF_8));
        when(encryptionService.decrypt(secondVersion.getValueCipher()))
                .thenReturn("beta".getBytes(StandardCharsets.UTF_8));

        var resolved = port.resolveSecrets(100L, List.of(firstSecret.getSecretUuid(), secondSecret.getSecretUuid()));

        assertThat(resolved).hasSize(2);
        assertThat(resolved.get(firstSecret.getSecretUuid())).isEqualTo("alpha");
        assertThat(resolved.get(secondSecret.getSecretUuid())).isEqualTo("beta");
    }

    private Secret secret(Long id, Long organizationId, String name) {
        return Secret.reconstitute(
                id,
                UUID.randomUUID(),
                organizationId,
                name,
                SecretScope.JOB,
                "description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private SecretVersion version(Long secretId, int version, String plaintext) {
        return SecretVersion.reconstitute(
                secretId + 1000,
                secretId,
                version,
                plaintext.getBytes(StandardCharsets.UTF_8),
                null,
                true,
                11L,
                Instant.parse("2024-01-01T02:00:00Z"),
                null
        );
    }
}
