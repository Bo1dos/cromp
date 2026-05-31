package com.cromp.secrets.api.mapper;

import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.model.enums.SecretScope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecretApiMapperTest {

    private final SecretApiMapper mapper = new SecretApiMapper();

    @Test
    void shouldMapSecretResponseWithCurrentVersionWhenActiveVersionExists() {
        Secret secret = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                42L,
                "my-secret",
                SecretScope.GLOBAL,
                "description",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                null
        );
        SecretVersion version = SecretVersion.reconstitute(
                7L,
                11L,
                4,
                new byte[]{1, 2, 3},
                null,
                true,
                99L,
                Instant.parse("2024-01-01T02:00:00Z"),
                null
        );

        var response = mapper.toSecretResponse(secret, version);

        assertThat(response.secretUuid()).isEqualTo(secret.getSecretUuid());
        assertThat(response.name()).isEqualTo("my-secret");
        assertThat(response.scope()).isEqualTo("GLOBAL");
        assertThat(response.description()).isEqualTo("description");
        assertThat(response.currentVersion()).isEqualTo(4);
        assertThat(response.createdAt()).isEqualTo(secret.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(secret.getUpdatedAt());
    }

    @Test
    void shouldMapSecretResponseWithZeroCurrentVersionWhenActiveVersionIsMissing() {
        Secret secret = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                42L,
                "my-secret",
                SecretScope.JOB,
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"),
                null
        );

        var response = mapper.toSecretResponse(secret, null);

        assertThat(response.currentVersion()).isZero();
    }

    @Test
    void shouldMapVersionResponseWithAllFieldsWhenCalled() {
        Instant createdAt = Instant.parse("2024-01-01T02:00:00Z");
        Instant deprecatedAt = Instant.parse("2024-01-01T03:00:00Z");
        SecretVersion version = SecretVersion.reconstitute(
                7L,
                11L,
                4,
                new byte[]{1, 2, 3},
                null,
                false,
                99L,
                createdAt,
                deprecatedAt
        );

        var response = mapper.toVersionResponse(version);

        assertThat(response.version()).isEqualTo(4);
        assertThat(response.isActive()).isFalse();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.deprecatedAt()).isEqualTo(deprecatedAt);
    }
}
