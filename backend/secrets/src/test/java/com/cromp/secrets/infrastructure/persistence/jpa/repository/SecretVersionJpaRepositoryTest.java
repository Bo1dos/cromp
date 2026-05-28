package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import com.cromp.iam.infrastructure.persistence.jpa.repository.OrganizationJpaRepository;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.infrastructure.persistence.SecretsPersistenceTestConfiguration;
import com.cromp.secrets.infrastructure.persistence.SecretsPostgresTestSupport;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretVersionJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@Import(SecretsPersistenceTestConfiguration.class)
class SecretVersionJpaRepositoryTest extends SecretsPostgresTestSupport {

    @Autowired
    private SecretVersionJpaRepository versionRepository;
    @Autowired
    private SecretJpaRepository secretRepository;
    @Autowired
    private OrganizationJpaRepository organizationRepository;

    @Test
    void shouldFindVersionBySecretIdAndVersionWhenExists() {
        SecretJpaEntity secret = seedSecret("api-key");
        SecretVersionJpaEntity saved = saveVersion(secret.getId(), 1, true);

        assertThat(versionRepository.findBySecretIdAndVersion(secret.getId(), 1))
                .get()
                .extracting(SecretVersionJpaEntity::getSecretId, SecretVersionJpaEntity::getVersion, SecretVersionJpaEntity::isActive)
                .containsExactly(saved.getSecretId(), 1, true);
    }

    @Test
    void shouldFindActiveVersionWhenOneExists() {
        SecretJpaEntity secret = seedSecret("api-key");
        saveVersion(secret.getId(), 1, false);
        SecretVersionJpaEntity active = saveVersion(secret.getId(), 2, true);

        assertThat(versionRepository.findActiveVersion(secret.getId()))
                .get()
                .extracting(SecretVersionJpaEntity::getSecretId, SecretVersionJpaEntity::getVersion, SecretVersionJpaEntity::isActive)
                .containsExactly(active.getSecretId(), 2, true);
    }

    @Test
    void shouldReturnVersionsOrderedByVersionDescending() {
        SecretJpaEntity secret = seedSecret("api-key");
        saveVersion(secret.getId(), 1, true);
        saveVersion(secret.getId(), 3, true);
        saveVersion(secret.getId(), 2, false);

        List<SecretVersionJpaEntity> versions = versionRepository.findBySecretIdOrderByVersionDesc(secret.getId());

        assertThat(versions).extracting(SecretVersionJpaEntity::getVersion).containsExactly(3, 2, 1);
    }

    @Test
    void shouldReturnMaxVersionWhenVersionsExist() {
        SecretJpaEntity secret = seedSecret("api-key");
        saveVersion(secret.getId(), 1, true);
        saveVersion(secret.getId(), 4, true);
        saveVersion(secret.getId(), 2, false);

        assertThat(versionRepository.getMaxVersion(secret.getId())).contains(4);
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoVersionsExist() {
        SecretJpaEntity secret = seedSecret("api-key");

        assertThat(versionRepository.findActiveVersion(secret.getId())).isEmpty();
        assertThat(versionRepository.getMaxVersion(secret.getId())).isEmpty();
    }

    private SecretJpaEntity seedSecret(String name) {
        Long organizationId = seedOrganization();
        return secretRepository.save(SecretJpaEntity.builder()
                .secretUuid(UUID.randomUUID())
                .organizationId(organizationId)
                .name(name)
                .scope(SecretScope.JOB)
                .description("description")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build());
    }

    private Long seedOrganization() {
        OrganizationJpaEntity organization = organizationRepository.save(OrganizationJpaEntity.builder()
                .orgUuid(UUID.randomUUID())
                .name("Acme")
                .settings(Map.of())
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build());
        return organization.getId();
    }

    private SecretVersionJpaEntity saveVersion(Long secretId, int version, boolean active) {
        return versionRepository.save(SecretVersionJpaEntity.builder()
                .secretId(secretId)
                .version(version)
                .valueCipher(new byte[]{1, 2, 3, (byte) version})
                .keyId("key")
                .active(active)
                .createdAt(Instant.parse("2024-01-01T01:00:00Z"))
                .build());
    }
}
