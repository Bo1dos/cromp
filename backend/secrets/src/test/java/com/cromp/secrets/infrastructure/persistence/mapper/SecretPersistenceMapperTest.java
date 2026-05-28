package com.cromp.secrets.infrastructure.persistence.mapper;

import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.enums.SecretScope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecretPersistenceMapperTest {

    private final SecretPersistenceMapper mapper = new SecretPersistenceMapper();

    @Test
    void shouldMapDomainToJpaAndBackWithoutLosingFields() {
        Secret original = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                22L,
                "api-key",
                SecretScope.GLOBAL,
                "description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                Instant.parse("2024-01-01T02:00:00Z")
        );

        var entity = mapper.toJpa(original);
        Secret restored = mapper.toDomain(entity);

        assertThat(entity.getSecretUuid()).isEqualTo(original.getSecretUuid());
        assertThat(entity.getScope()).isEqualTo(SecretScope.GLOBAL);
        assertThat(restored.getSecretUuid()).isEqualTo(original.getSecretUuid());
        assertThat(restored.getOrganizationId()).isEqualTo(22L);
        assertThat(restored.getName()).isEqualTo("api-key");
        assertThat(restored.getDescription()).isEqualTo("description");
        assertThat(restored.getDeletedAt()).isEqualTo(original.getDeletedAt());
    }
}
