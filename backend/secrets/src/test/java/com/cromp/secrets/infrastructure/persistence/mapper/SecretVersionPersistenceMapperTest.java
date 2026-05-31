package com.cromp.secrets.infrastructure.persistence.mapper;

import com.cromp.secrets.domain.model.SecretVersion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SecretVersionPersistenceMapperTest {

    private final SecretVersionPersistenceMapper mapper = new SecretVersionPersistenceMapper();

    @Test
    void shouldMapDomainToJpaAndBackWithoutLosingFields() {
        SecretVersion original = SecretVersion.reconstitute(
                11L,
                22L,
                3,
                new byte[]{1, 2, 3},
                "key-1",
                false,
                44L,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z")
        );

        var entity = mapper.toJpa(original);
        SecretVersion restored = mapper.toDomain(entity);

        assertThat(entity.getSecretId()).isEqualTo(22L);
        assertThat(entity.getValueCipher()).containsExactly(1, 2, 3);
        assertThat(restored.getSecretId()).isEqualTo(22L);
        assertThat(restored.getVersion()).isEqualTo(3);
        assertThat(restored.getKeyId()).isEqualTo("key-1");
        assertThat(restored.isActive()).isFalse();
        assertThat(restored.getCreatedBy()).isEqualTo(44L);
    }
}
