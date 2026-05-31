package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import com.cromp.iam.infrastructure.persistence.jpa.repository.OrganizationJpaRepository;
import com.cromp.secrets.infrastructure.persistence.SecretsPersistenceTestConfiguration;
import com.cromp.secrets.infrastructure.persistence.SecretsPostgresTestSupport;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import com.cromp.secrets.domain.model.enums.SecretScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@Import(SecretsPersistenceTestConfiguration.class)
class SecretJpaRepositoryTest extends SecretsPostgresTestSupport {

    @Autowired
    private SecretJpaRepository secretRepository;
    @Autowired
    private OrganizationJpaRepository organizationRepository;

    @Test
    void shouldFindSecretByUuidWhenSecretExists() {
        Long organizationId = seedOrganization();
        SecretJpaEntity saved = saveSecret(organizationId, "api-key", null);

        assertThat(secretRepository.findBySecretUuid(saved.getSecretUuid())).isPresent();
        assertThat(secretRepository.findBySecretUuid(saved.getSecretUuid()).orElseThrow().getName()).isEqualTo("api-key");
    }

    @Test
    void shouldFindSecretByNameAndOrganizationIdWhenSecretIsNotDeleted() {
        Long organizationId = seedOrganization();
        SecretJpaEntity saved = saveSecret(organizationId, "api-key", null);

        assertThat(secretRepository.findByNameAndOrganizationIdAndDeletedAtIsNull("api-key", organizationId))
                .get()
                .extracting(SecretJpaEntity::getId, SecretJpaEntity::getSecretUuid, SecretJpaEntity::getName)
                .containsExactly(saved.getId(), saved.getSecretUuid(), "api-key");
    }

    @Test
    void shouldReturnOnlyNonDeletedSecretsForOrganization() {
        Long organizationId = seedOrganization();
        saveSecret(organizationId, "active", null);
        saveSecret(organizationId, "deleted", Instant.parse("2024-01-01T02:00:00Z"));

        List<SecretJpaEntity> secrets = secretRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId);

        assertThat(secrets).extracting(SecretJpaEntity::getName).containsExactly("active");
    }

    @Test
    void shouldNotReturnSoftDeletedSecretInQueryMethods() {
        Long organizationId = seedOrganization();
        SecretJpaEntity deleted = saveSecret(organizationId, "secret", Instant.parse("2024-01-01T02:00:00Z"));

        assertThat(secretRepository.findByNameAndOrganizationIdAndDeletedAtIsNull("secret", organizationId)).isEmpty();
        assertThat(secretRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId)).isEmpty();
        assertThat(secretRepository.findBySecretUuid(deleted.getSecretUuid())).isEmpty();
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

    private SecretJpaEntity saveSecret(Long organizationId, String name, Instant deletedAt) {
        return secretRepository.save(SecretJpaEntity.builder()
                .secretUuid(UUID.randomUUID())
                .organizationId(organizationId)
                .name(name)
                .scope(SecretScope.JOB)
                .description("description")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .deletedAt(deletedAt)
                .build());
    }
}
