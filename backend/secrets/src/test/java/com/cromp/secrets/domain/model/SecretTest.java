package com.cromp.secrets.domain.model;

import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.support.SchemaLimits;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretTest {

    @Test
    void shouldCreateAndNormalizeFieldsWhenValidDataProvided() {
        Secret secret = Secret.create(42L, "  my-secret  ", SecretScope.JOB, "  description  ");

        assertThat(secret.getSecretUuid()).isNotNull();
        assertThat(secret.getOrganizationId()).isEqualTo(42L);
        assertThat(secret.getName()).isEqualTo("my-secret");
        assertThat(secret.getScope()).isEqualTo(SecretScope.JOB);
        assertThat(secret.getDescription()).isEqualTo("description");
        assertThat(secret.getCreatedAt()).isNotNull();
        assertThat(secret.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldReconstituteAndPreserveFieldsWhenValidDataProvided() {
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-01-01T01:00:00Z");
        Instant deletedAt = Instant.parse("2024-01-01T02:00:00Z");
        UUID secretUuid = UUID.randomUUID();

        Secret secret = Secret.reconstitute(
                11L,
                secretUuid,
                42L,
                "my-secret",
                SecretScope.ORGANIZATION,
                "description",
                createdAt,
                updatedAt,
                deletedAt
        );

        assertThat(secret.getId()).isEqualTo(11L);
        assertThat(secret.getSecretUuid()).isEqualTo(secretUuid);
        assertThat(secret.getOrganizationId()).isEqualTo(42L);
        assertThat(secret.getName()).isEqualTo("my-secret");
        assertThat(secret.getScope()).isEqualTo(SecretScope.ORGANIZATION);
        assertThat(secret.getDescription()).isEqualTo("description");
        assertThat(secret.getCreatedAt()).isEqualTo(createdAt);
        assertThat(secret.getUpdatedAt()).isEqualTo(updatedAt);
        assertThat(secret.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(secret.isDeleted()).isTrue();
    }

    @Test
    void shouldUpdateNameAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Secret secret = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                42L,
                "my-secret",
                SecretScope.JOB,
                "description",
                before,
                before,
                null
        );

        secret.updateName("  rotated-name  ");

        assertThat(secret.getName()).isEqualTo("rotated-name");
        assertThat(secret.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldUpdateDescriptionAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Secret secret = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                42L,
                "my-secret",
                SecretScope.JOB,
                "description",
                before,
                before,
                null
        );

        secret.updateDescription("  new description  ");

        assertThat(secret.getDescription()).isEqualTo("new description");
        assertThat(secret.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldMarkDeletedAndRestoreWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Secret secret = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                42L,
                "my-secret",
                SecretScope.JOB,
                "description",
                before,
                before,
                null
        );

        secret.markDeleted();
        assertThat(secret.isDeleted()).isTrue();
        assertThat(secret.getDeletedAt()).isNotNull();

        secret.restore();
        assertThat(secret.isDeleted()).isFalse();
        assertThat(secret.getDeletedAt()).isNull();
    }

    @Test
    void shouldTouchUpdatedAtWhenUpdateTimestampIsCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Secret secret = Secret.reconstitute(
                11L,
                UUID.randomUUID(),
                42L,
                "my-secret",
                SecretScope.JOB,
                "description",
                before,
                before,
                null
        );

        secret.updateTimestamp();

        assertThat(secret.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldRejectBlankNameAndMissingMandatoryFieldsWhenCreating() {
        assertThatThrownBy(() -> Secret.create(42L, "   ", SecretScope.JOB, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be blank");

        assertThatThrownBy(() -> Secret.create(null, "secret", SecretScope.JOB, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("organizationId must not be null");

        assertThatThrownBy(() -> Secret.create(42L, "secret", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scope must not be null");
    }

    @Test
    void shouldRejectTooLongValuesWhenCreatingOrUpdating() {
        String tooLongName = "a".repeat(SchemaLimits.SECRET_NAME_MAX_LENGTH + 1);
        String tooLongDescription = "a".repeat(SchemaLimits.SECRET_DESCRIPTION_MAX_LENGTH + 1);

        assertThatThrownBy(() -> Secret.create(42L, tooLongName, SecretScope.JOB, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not exceed");

        Secret secret = Secret.create(42L, "secret", SecretScope.JOB, null);

        assertThatThrownBy(() -> secret.updateName(tooLongName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not exceed");

        assertThatThrownBy(() -> secret.updateDescription(tooLongDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("description must not exceed");
    }
}
