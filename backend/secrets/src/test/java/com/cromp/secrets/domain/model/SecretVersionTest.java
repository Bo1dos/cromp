package com.cromp.secrets.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecretVersionTest {

    @Test
    void shouldCreateNewVersionWhenValidDataProvided() {
        Instant before = Instant.now();

        SecretVersion version = SecretVersion.createNew(11L, 1, new byte[]{1, 2, 3}, "key-1", 99L);

        assertThat(version.getId()).isNull();
        assertThat(version.getSecretId()).isEqualTo(11L);
        assertThat(version.getVersion()).isEqualTo(1);
        assertThat(version.getValueCipher()).containsExactly(1, 2, 3);
        assertThat(version.getKeyId()).isEqualTo("key-1");
        assertThat(version.isActive()).isTrue();
        assertThat(version.getCreatedBy()).isEqualTo(99L);
        assertThat(version.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(version.getDeprecatedAt()).isNull();
    }

    @Test
    void shouldReconstituteAndPreserveFieldsWhenValidDataProvided() {
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant deprecatedAt = Instant.parse("2024-01-01T01:00:00Z");
        byte[] valueCipher = new byte[]{4, 5, 6};

        SecretVersion version = SecretVersion.reconstitute(
                7L,
                11L,
                3,
                valueCipher,
                "key-2",
                false,
                12L,
                createdAt,
                deprecatedAt
        );

        assertThat(version.getId()).isEqualTo(7L);
        assertThat(version.getSecretId()).isEqualTo(11L);
        assertThat(version.getVersion()).isEqualTo(3);
        assertThat(version.getValueCipher()).isSameAs(valueCipher);
        assertThat(version.getKeyId()).isEqualTo("key-2");
        assertThat(version.isActive()).isFalse();
        assertThat(version.getCreatedBy()).isEqualTo(12L);
        assertThat(version.getCreatedAt()).isEqualTo(createdAt);
        assertThat(version.getDeprecatedAt()).isEqualTo(deprecatedAt);
    }

    @Test
    void shouldDeprecateActiveVersionWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        SecretVersion version = SecretVersion.reconstitute(
                7L,
                11L,
                3,
                new byte[]{4, 5, 6},
                "key-2",
                true,
                12L,
                before,
                null
        );

        version.deprecate();

        assertThat(version.isActive()).isFalse();
        assertThat(version.getDeprecatedAt()).isAfter(before);
    }

    @Test
    void shouldThrowWhenDeprecatingAlreadyDeprecatedVersion() {
        SecretVersion version = SecretVersion.reconstitute(
                7L,
                11L,
                3,
                new byte[]{4, 5, 6},
                "key-2",
                false,
                12L,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z")
        );

        assertThatThrownBy(version::deprecate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already deprecated");
    }

    @Test
    void shouldRejectMissingMandatoryFieldsWhenCreating() {
        assertThatThrownBy(() -> SecretVersion.createNew(null, 1, new byte[]{1}, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("secretId must not be null");

        assertThatThrownBy(() -> SecretVersion.createNew(1L, 1, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valueCipher must not be null");
    }
}
