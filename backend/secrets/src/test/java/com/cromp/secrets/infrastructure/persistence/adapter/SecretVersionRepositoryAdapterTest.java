package com.cromp.secrets.infrastructure.persistence.adapter;

import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import com.cromp.iam.infrastructure.persistence.jpa.repository.OrganizationJpaRepository;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.infrastructure.persistence.SecretsPersistenceTestConfiguration;
import com.cromp.secrets.infrastructure.persistence.SecretsPostgresTestSupport;
import com.cromp.secrets.infrastructure.persistence.mapper.SecretPersistenceMapper;
import com.cromp.secrets.infrastructure.persistence.mapper.SecretVersionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@Import({
        SecretsPersistenceTestConfiguration.class,
        SecretPersistenceMapper.class,
        SecretVersionPersistenceMapper.class,
        SecretRepositoryAdapter.class,
        SecretVersionRepositoryAdapter.class
})
class SecretVersionRepositoryAdapterTest extends SecretsPostgresTestSupport {

    @Autowired
    private SecretVersionRepositoryAdapter adapter;
    @Autowired
    private SecretRepositoryAdapter secretRepositoryAdapter;
    @Autowired
    private OrganizationJpaRepository organizationRepository;

    @Test
    void shouldSaveAndQuerySecretVersionsThroughAdapterWhenCalled() {
        Secret secret = seedSecret();
        SecretVersion first = adapter.save(SecretVersion.createNew(secret.getId(), 1, new byte[]{1}, null, 11L));
        SecretVersion second = adapter.save(SecretVersion.createNew(secret.getId(), 2, new byte[]{2}, null, 11L));
        SecretVersion active = adapter.save(SecretVersion.createNew(secret.getId(), 3, new byte[]{3}, null, 11L));

        assertThat(adapter.findBySecretIdAndVersion(secret.getId(), 2))
                .get()
                .extracting(SecretVersion::getSecretId, SecretVersion::getVersion, SecretVersion::isActive)
                .containsExactly(secret.getId(), 2, true);
        assertThat(adapter.findActiveVersion(secret.getId()))
                .get()
                .extracting(SecretVersion::getSecretId, SecretVersion::getVersion, SecretVersion::isActive)
                .containsExactly(secret.getId(), 3, true);
        assertThat(adapter.findBySecretIdOrderByVersionDesc(secret.getId()))
                .extracting(SecretVersion::getVersion)
                .containsExactly(3, 2, 1);
        assertThat(adapter.getMaxVersion(secret.getId())).contains(3);
        assertThat(first.getSecretId()).isEqualTo(secret.getId());
    }

    @Test
    void shouldReturnEmptyWhenSecretHasNoVersionsThroughAdapter() {
        Secret secret = seedSecret();

        assertThat(adapter.findActiveVersion(secret.getId())).isEmpty();
        assertThat(adapter.findBySecretIdOrderByVersionDesc(secret.getId())).isEmpty();
        assertThat(adapter.getMaxVersion(secret.getId())).isEmpty();
    }

    private Secret seedSecret() {
        Long organizationId = seedOrganization();
        Secret domainSecret = Secret.create(organizationId, "api-key", SecretScope.JOB, "description");
        return secretRepositoryAdapter.save(domainSecret);
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
