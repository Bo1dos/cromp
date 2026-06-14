package com.cromp.secrets.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Secret rotation")
class SecretRotationTest extends SecretsPostgresTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("should rotate secret: deprecate old version and activate new one")
    void shouldRotateSecret() {
        Long orgId = seedOrg();
        Long secretId = seedSecret(orgId, "api-token");
        seedSecretVersion(secretId, 1, "enc-v1", true);
        seedSecretVersion(secretId, 2, "enc-v2", true);

        // deprecate old version
        jdbcTemplate.update(
                "update secret_versions set active = false, deprecated_at = ? where secret_id = ? and version = 1",
                Instant.now(), secretId
        );

        var rows = jdbcTemplate.queryForList(
                "select version, active from secret_versions where secret_id = ? order by version",
                secretId
        );
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).get("active")).isEqualTo(false);
        assertThat(rows.get(1).get("active")).isEqualTo(true);
    }

    @Test
    @DisplayName("should keep old version value after rotation")
    void shouldKeepOldVersionValue() {
        Long orgId = seedOrg();
        Long secretId = seedSecret(orgId, "db-pass");
        seedSecretVersion(secretId, 1, "cipher:v1:abc", true);
        seedSecretVersion(secretId, 2, "cipher:v2:xyz", true);
        jdbcTemplate.update(
                "update secret_versions set active = false, deprecated_at = ? where secret_id = ? and version = 1",
                Instant.now(), secretId
        );

        List<String> values = jdbcTemplate.queryForList(
                "select value_cipher from secret_versions where secret_id = ? order by version",
                String.class, secretId
        );
        assertThat(values).containsExactly("cipher:v1:abc", "cipher:v2:xyz");
    }

    @Test
    @DisplayName("should track secret access during execution")
    void shouldTrackSecretAccess() {
        Long orgId = seedOrg();
        Long secretId = seedSecret(orgId, "svc-key");
        seedSecretVersion(secretId, 1, "encrypted", true);

        UUID attemptUuid = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into execution_secret_access (attempt_uuid, secret_id, secret_version_id, accessed_at) values (?, ?, ?, ?)",
                attemptUuid, secretId, 1L, Instant.parse("2024-01-01T00:00:00Z")
        );

        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from execution_secret_access where attempt_uuid = ?",
                Integer.class, attemptUuid
        );
        assertThat(count).isEqualTo(1);
    }

    private Long seedOrg() {
        return jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class, UUID.randomUUID(), "o-" + UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
    }

    private Long seedSecret(Long orgId, String name) {
        return jdbcTemplate.queryForObject(
                "insert into secrets (secret_uuid, organization_id, name, scope, created_at, updated_at) values (?, ?, ?, 'ORGANIZATION', ?, ?) returning id",
                Long.class, UUID.randomUUID(), orgId, name,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
    }

    private void seedSecretVersion(Long secretId, int version, String valueCipher, boolean active) {
        jdbcTemplate.update(
                "insert into secret_versions (secret_id, version, value_cipher, active, created_at) values (?, ?, ?, ?, ?)",
                secretId, version, valueCipher, active, Instant.parse("2024-01-01T00:00:00Z"));
    }
}
