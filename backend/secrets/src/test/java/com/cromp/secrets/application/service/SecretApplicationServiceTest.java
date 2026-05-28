package com.cromp.secrets.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.secrets.api.dto.request.CreateSecretRequest;
import com.cromp.secrets.api.dto.request.RotateSecretRequest;
import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.api.mapper.SecretApiMapper;
import com.cromp.secrets.application.port.AuditPort;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.exceptions.InvalidSecretStateException;
import com.cromp.secrets.domain.model.exceptions.SecretNotFoundException;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import com.cromp.secrets.domain.repository.SecretVersionRepositoryPort;
import com.cromp.secrets.domain.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretApplicationServiceTest {

    private static final Long USER_ID = 11L;
    private static final Long ORGANIZATION_ID = 22L;

    @Mock
    private SecretRepositoryPort secretRepository;
    @Mock
    private SecretVersionRepositoryPort versionRepository;
    @Mock
    private EncryptionService encryptionService;
    @Mock
    private AuditPort auditPort;
    @Mock
    private CurrentActorPort currentActorPort;
    @Mock
    private PermissionCheckerPort permissionCheckerPort;

    private SecretApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SecretApplicationService(
                secretRepository,
                versionRepository,
                encryptionService,
                new SecretApiMapper(),
                auditPort,
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldCreateSecretWhenScopeIsMissingUsesDefaultJob() {
        CreateSecretRequest request = new CreateSecretRequest("api-key", "plain-text", null, "  payment token  ");
        Secret persistedSecret = secretTemplate("api-key", SecretScope.JOB, "payment token");
        Secret savedSecret = reconstituteSecret(100L, persistedSecret);
        byte[] encrypted = new byte[]{9, 9, 9};

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:create")).thenReturn(true);
        when(secretRepository.findByNameAndOrganizationId("api-key", ORGANIZATION_ID)).thenReturn(Optional.empty());
        when(secretRepository.save(any(Secret.class))).thenReturn(savedSecret);
        when(encryptionService.encrypt("plain-text".getBytes(StandardCharsets.UTF_8))).thenReturn(encrypted);
        when(versionRepository.save(any(SecretVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecretResponse response = service.createSecret(request);

        ArgumentCaptor<Secret> secretCaptor = ArgumentCaptor.forClass(Secret.class);
        verify(secretRepository).save(secretCaptor.capture());
        assertThat(secretCaptor.getValue().getScope()).isEqualTo(SecretScope.JOB);
        assertThat(secretCaptor.getValue().getDescription()).isEqualTo("payment token");

        ArgumentCaptor<SecretVersion> versionCaptor = ArgumentCaptor.forClass(SecretVersion.class);
        verify(versionRepository).save(versionCaptor.capture());
        assertThat(versionCaptor.getValue().getSecretId()).isEqualTo(100L);
        assertThat(versionCaptor.getValue().getVersion()).isEqualTo(1);
        assertThat(versionCaptor.getValue().isActive()).isTrue();
        assertThat(versionCaptor.getValue().getCreatedBy()).isEqualTo(USER_ID);

        verify(encryptionService).encrypt("plain-text".getBytes(StandardCharsets.UTF_8));
        verify(auditPort).record("SECRET.CREATE", ORGANIZATION_ID, USER_ID, "secrets", 100L, Map.of("name", "api-key"));

        assertThat(response.secretUuid()).isEqualTo(savedSecret.getSecretUuid());
        assertThat(response.name()).isEqualTo("api-key");
        assertThat(response.scope()).isEqualTo("JOB");
        assertThat(response.description()).isEqualTo("payment token");
        assertThat(response.currentVersion()).isEqualTo(1);
    }

    @Test
    void shouldCreateSecretWhenScopeIsProvidedUsesIt() {
        CreateSecretRequest request = new CreateSecretRequest("api-key", "plain-text", SecretScope.GLOBAL, null);
        Secret savedSecret = reconstituteSecret(100L, Secret.create(ORGANIZATION_ID, "api-key", SecretScope.GLOBAL, null));

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:create")).thenReturn(true);
        when(secretRepository.findByNameAndOrganizationId("api-key", ORGANIZATION_ID)).thenReturn(Optional.empty());
        when(secretRepository.save(any(Secret.class))).thenReturn(savedSecret);
        when(encryptionService.encrypt(any(byte[].class))).thenReturn(new byte[]{1});
        when(versionRepository.save(any(SecretVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecretResponse response = service.createSecret(request);

        ArgumentCaptor<Secret> secretCaptor = ArgumentCaptor.forClass(Secret.class);
        verify(secretRepository).save(secretCaptor.capture());
        assertThat(secretCaptor.getValue().getScope()).isEqualTo(SecretScope.GLOBAL);
        assertThat(response.scope()).isEqualTo("GLOBAL");
    }

    @Test
    void shouldThrowWhenCreatingSecretAlreadyExists() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:create")).thenReturn(true);
        when(secretRepository.findByNameAndOrganizationId("api-key", ORGANIZATION_ID))
                .thenReturn(Optional.of(secretTemplate("api-key", SecretScope.JOB, null)));

        assertThatThrownBy(() -> service.createSecret(new CreateSecretRequest("api-key", "plain-text", null, null)))
                .isInstanceOf(InvalidSecretStateException.class)
                .hasMessageContaining("already exists");
        verifyNoInteractions(encryptionService, versionRepository, auditPort);
    }

    @Test
    void shouldThrowWhenCreatingSecretWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSecret(new CreateSecretRequest("api-key", "plain-text", null, null)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenCreatingSecretWithoutOrganization() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(USER_ID));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSecret(new CreateSecretRequest("api-key", "plain-text", null, null)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not in an organization");
    }

    @Test
    void shouldThrowWhenCreatingSecretWithoutPermission() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:create")).thenReturn(false);

        assertThatThrownBy(() -> service.createSecret(new CreateSecretRequest("api-key", "plain-text", null, null)))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldReturnSecretWhenActiveVersionExists() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");
        SecretVersion version = versionTemplate(7L, secret.getId(), 3, true, null);

        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid())).thenReturn(Optional.of(secret));
        when(versionRepository.findActiveVersion(secret.getId())).thenReturn(Optional.of(version));

        SecretResponse response = service.getSecret(secret.getSecretUuid());

        assertThat(response.name()).isEqualTo("api-key");
        assertThat(response.currentVersion()).isEqualTo(3);
        assertThat(response.scope()).isEqualTo("JOB");
    }

    @Test
    void shouldReturnSecretWithZeroCurrentVersionWhenActiveVersionIsMissing() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid())).thenReturn(Optional.of(secret));
        when(versionRepository.findActiveVersion(secret.getId())).thenReturn(Optional.empty());

        SecretResponse response = service.getSecret(secret.getSecretUuid());

        assertThat(response.currentVersion()).isZero();
    }

    @Test
    void shouldThrowWhenGettingSecretWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSecret(UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenGettingSecretWithoutOrganization() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(USER_ID));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSecret(UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not in an organization");
    }

    @Test
    void shouldThrowWhenGettingSecretFromForeignOrganization() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid()))
                .thenReturn(Optional.of(reconstituteSecret(100L, secret, ORGANIZATION_ID + 1)));

        assertThatThrownBy(() -> service.getSecret(secret.getSecretUuid()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void shouldThrowWhenGettingSecretThatDoesNotExist() {
        UUID secretUuid = UUID.randomUUID();
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secretUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSecret(secretUuid))
                .isInstanceOf(SecretNotFoundException.class)
                .hasMessageContaining("secretUuid");
    }

    @Test
    void shouldReturnSecretsForCurrentOrganization() {
        Secret firstSecret = secretTemplate(101L, "first", SecretScope.JOB, "description");
        Secret secondSecret = secretTemplate(102L, "second", SecretScope.GLOBAL, "description");
        SecretVersion firstVersion = versionTemplate(7L, firstSecret.getId(), 2, true, null);
        SecretVersion secondVersion = versionTemplate(8L, secondSecret.getId(), 1, true, null);

        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findByOrganizationId(ORGANIZATION_ID)).thenReturn(List.of(firstSecret, secondSecret));
        when(versionRepository.findActiveVersion(firstSecret.getId())).thenReturn(Optional.of(firstVersion));
        when(versionRepository.findActiveVersion(secondSecret.getId())).thenReturn(Optional.of(secondVersion));

        List<SecretResponse> responses = service.listSecrets();

        assertThat(responses).extracting(SecretResponse::name).containsExactly("first", "second");
        assertThat(responses).extracting(SecretResponse::currentVersion).containsExactly(2, 1);
    }

    @Test
    void shouldReturnEmptyListWhenNoSecretsExist() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findByOrganizationId(ORGANIZATION_ID)).thenReturn(List.of());

        assertThat(service.listSecrets()).isEmpty();
    }

    @Test
    void shouldThrowWhenListingSecretsWithoutMembership() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.listSecrets())
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not a member");
    }

    @Test
    void shouldThrowWhenListingSecretsWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listSecrets())
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldDeleteSecretAndRecordAuditWhenAllowed() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:delete")).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid())).thenReturn(Optional.of(secret));
        when(secretRepository.save(any(Secret.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deleteSecret(secret.getSecretUuid());

        ArgumentCaptor<Secret> secretCaptor = ArgumentCaptor.forClass(Secret.class);
        verify(secretRepository).save(secretCaptor.capture());
        assertThat(secretCaptor.getValue().isDeleted()).isTrue();
        verify(auditPort).record("SECRET.DELETE", ORGANIZATION_ID, USER_ID, "secrets", secret.getId(),
                Map.of("secretUuid", secret.getSecretUuid().toString()));
    }

    @Test
    void shouldThrowWhenDeletingSecretWithoutPermission() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:delete")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteSecret(UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldThrowWhenDeletingSecretThatDoesNotExist() {
        UUID secretUuid = UUID.randomUUID();
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:delete")).thenReturn(true);
        when(secretRepository.findBySecretUuid(secretUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteSecret(secretUuid))
                .isInstanceOf(SecretNotFoundException.class);
    }

    @Test
    void shouldThrowWhenDeletingSecretFromForeignOrganization() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:delete")).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid()))
                .thenReturn(Optional.of(reconstituteSecret(100L, secret, ORGANIZATION_ID + 1)));

        assertThatThrownBy(() -> service.deleteSecret(secret.getSecretUuid()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void shouldRotateSecretAndDeprecatePreviousVersion() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");
        Instant before = secret.getUpdatedAt();
        SecretVersion activeVersion = versionTemplate(7L, secret.getId(), 2, true, null);

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:update")).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid())).thenReturn(Optional.of(secret));
        when(versionRepository.findActiveVersion(secret.getId())).thenReturn(Optional.of(activeVersion));
        when(versionRepository.getMaxVersion(secret.getId())).thenReturn(Optional.of(2));
        when(encryptionService.encrypt("rotated".getBytes(StandardCharsets.UTF_8))).thenReturn(new byte[]{8, 8, 8});
        when(versionRepository.save(any(SecretVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretRepository.save(any(Secret.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecretResponse response = service.rotateSecret(secret.getSecretUuid(), new RotateSecretRequest("rotated"));

        ArgumentCaptor<SecretVersion> versionCaptor = ArgumentCaptor.forClass(SecretVersion.class);
        verify(versionRepository, org.mockito.Mockito.times(2)).save(versionCaptor.capture());
        assertThat(versionCaptor.getAllValues()).hasSize(2);
        assertThat(versionCaptor.getAllValues().get(0).isActive()).isFalse();
        assertThat(versionCaptor.getAllValues().get(0).getDeprecatedAt()).isNotNull();
        assertThat(versionCaptor.getAllValues().get(1).getVersion()).isEqualTo(3);
        assertThat(versionCaptor.getAllValues().get(1).isActive()).isTrue();
        assertThat(versionCaptor.getAllValues().get(1).getCreatedBy()).isEqualTo(USER_ID);

        ArgumentCaptor<Secret> secretCaptor = ArgumentCaptor.forClass(Secret.class);
        verify(secretRepository).save(secretCaptor.capture());
        assertThat(secretCaptor.getValue().getUpdatedAt()).isAfter(before);
        verify(auditPort).record("SECRET.ROTATE", ORGANIZATION_ID, USER_ID, "secrets", secret.getId(),
                Map.of("newVersion", 3));

        assertThat(response.currentVersion()).isEqualTo(3);
    }

    @Test
    void shouldRotateSecretStartingFromVersionOneWhenHistoryIsEmpty() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:update")).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid())).thenReturn(Optional.of(secret));
        when(versionRepository.findActiveVersion(secret.getId())).thenReturn(Optional.empty());
        when(versionRepository.getMaxVersion(secret.getId())).thenReturn(Optional.empty());
        when(encryptionService.encrypt("rotated".getBytes(StandardCharsets.UTF_8))).thenReturn(new byte[]{8, 8, 8});
        when(versionRepository.save(any(SecretVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(secretRepository.save(any(Secret.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecretResponse response = service.rotateSecret(secret.getSecretUuid(), new RotateSecretRequest("rotated"));

        ArgumentCaptor<SecretVersion> versionCaptor = ArgumentCaptor.forClass(SecretVersion.class);
        verify(versionRepository).save(versionCaptor.capture());
        assertThat(versionCaptor.getValue().getVersion()).isEqualTo(1);
        assertThat(response.currentVersion()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenRotatingSecretWithoutPermission() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:update")).thenReturn(false);

        assertThatThrownBy(() -> service.rotateSecret(UUID.randomUUID(), new RotateSecretRequest("rotated")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldThrowWhenRotatingSecretFromForeignOrganization() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "secret:update")).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid()))
                .thenReturn(Optional.of(reconstituteSecret(100L, secret, ORGANIZATION_ID + 1)));

        assertThatThrownBy(() -> service.rotateSecret(secret.getSecretUuid(), new RotateSecretRequest("rotated")))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void shouldThrowWhenGettingVersionsFromForeignOrganization() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");

        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid()))
                .thenReturn(Optional.of(reconstituteSecret(100L, secret, ORGANIZATION_ID + 1)));

        assertThatThrownBy(() -> service.getVersions(secret.getSecretUuid()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void shouldReturnVersionsInDescendingOrder() {
        Secret secret = secretTemplate("api-key", SecretScope.JOB, "description");
        SecretVersion first = versionTemplate(7L, secret.getId(), 3, true, null);
        SecretVersion second = versionTemplate(8L, secret.getId(), 2, false, Instant.parse("2024-01-01T05:00:00Z"));

        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secret.getSecretUuid())).thenReturn(Optional.of(secret));
        when(versionRepository.findBySecretIdOrderByVersionDesc(secret.getId())).thenReturn(List.of(first, second));

        List<SecretVersionResponse> responses = service.getVersions(secret.getSecretUuid());

        assertThat(responses).extracting(SecretVersionResponse::version).containsExactly(3, 2);
        assertThat(responses).extracting(SecretVersionResponse::isActive).containsExactly(true, false);
    }

    @Test
    void shouldThrowWhenGettingVersionsForMissingSecret() {
        UUID secretUuid = UUID.randomUUID();
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(secretRepository.findBySecretUuid(secretUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getVersions(secretUuid))
                .isInstanceOf(SecretNotFoundException.class);
    }

    private void authenticate() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(USER_ID));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(ORGANIZATION_ID));
    }

    private Secret secretTemplate(String name, SecretScope scope, String description) {
        return secretTemplate(100L, name, scope, description);
    }

    private Secret secretTemplate(Long id, String name, SecretScope scope, String description) {
        return Secret.reconstitute(
                id,
                UUID.randomUUID(),
                ORGANIZATION_ID,
                name,
                scope,
                description,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"),
                null
        );
    }

    private Secret reconstituteSecret(Long id, Secret source) {
        return reconstituteSecret(id, source, source.getOrganizationId());
    }

    private Secret reconstituteSecret(Long id, Secret source, Long organizationId) {
        return Secret.reconstitute(
                id,
                source.getSecretUuid(),
                organizationId,
                source.getName(),
                source.getScope(),
                source.getDescription(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getDeletedAt()
        );
    }

    private SecretVersion versionTemplate(Long id, Long secretId, int version, boolean active, Instant deprecatedAt) {
        return SecretVersion.reconstitute(
                id,
                secretId,
                version,
                new byte[]{1, 2, 3},
                null,
                active,
                USER_ID,
                Instant.parse("2024-01-01T02:00:00Z"),
                deprecatedAt
        );
    }
}
