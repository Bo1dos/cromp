package com.cromp.secrets.infrastructure.persistence.adapter;

import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import com.cromp.iam.infrastructure.persistence.jpa.repository.OrganizationJpaRepository;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.infrastructure.persistence.SecretsPersistenceTestConfiguration;
import com.cromp.secrets.infrastructure.persistence.SecretsPostgresTestSupport;
import com.cromp.secrets.infrastructure.persistence.mapper.SecretPersistenceMapper;
import com.cromp.secrets.infrastructure.persistence.jpa.repository.SecretJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@Import({
        SecretsPersistenceTestConfiguration.class,
        SecretPersistenceMapper.class,
        SecretRepositoryAdapter.class
})
class SecretRepositoryAdapterTest extends SecretsPostgresTestSupport {

    @Autowired
    private SecretRepositoryAdapter adapter;
    @Autowired
    private OrganizationJpaRepository organizationRepository;
    @Autowired
    private SecretJpaRepository secretJpaRepository;

    @Test
    void shouldSaveAndLoadSecretThroughAdapterWhenCalled() {
        Long organizationId = seedOrganization();
        Secret secret = Secret.create(organizationId, "api-key", SecretScope.JOB, "description");

        Secret saved = adapter.save(secret);

        assertThat(saved.getId()).isNotNull();
        assertThat(adapter.findBySecretUuid(saved.getSecretUuid()))
                .get()
                .extracting(Secret::getSecretUuid, Secret::getName, Secret::getScope)
                .containsExactly(saved.getSecretUuid(), "api-key", SecretScope.JOB);
        assertThat(adapter.findByNameAndOrganizationId("api-key", organizationId))
                .get()
                .extracting(Secret::getSecretUuid, Secret::getName, Secret::getScope)
                .containsExactly(saved.getSecretUuid(), "api-key", SecretScope.JOB);
        assertThat(adapter.findByOrganizationId(organizationId))
                .singleElement()
                .extracting(Secret::getSecretUuid, Secret::getName, Secret::getScope)
                .containsExactly(saved.getSecretUuid(), "api-key", SecretScope.JOB);
    }

    @Test
    void shouldFilterSoftDeletedSecretThroughAdapterWhenSavedWithDeletedAt() {
        Long organizationId = seedOrganization();
        Secret secret = Secret.create(organizationId, "api-key", SecretScope.JOB, "description");
        Secret saved = adapter.save(secret);

        Secret deleted = Secret.reconstitute(
                saved.getId(),
                saved.getSecretUuid(),
                saved.getOrganizationId(),
                saved.getName(),
                saved.getScope(),
                saved.getDescription(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                Instant.parse("2024-01-01T02:00:00Z")
        );
        adapter.save(deleted);

        assertThat(adapter.findBySecretUuid(saved.getSecretUuid())).isEmpty();
        assertThat(adapter.findByNameAndOrganizationId("api-key", organizationId)).isEmpty();
        assertThat(adapter.findByOrganizationId(organizationId)).isEmpty();
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
}
