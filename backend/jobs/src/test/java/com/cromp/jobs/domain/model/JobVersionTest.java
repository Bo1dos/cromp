package com.cromp.jobs.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobVersionTest {

    @Test
    void shouldCreateVersionWithCurrentTimestampAndStableValues() {
        JobVersion version = JobVersion.create(10L, 1, sampleConfig(), 42L);

        assertThat(version.getId()).isNull();
        assertThat(version.getJobId()).isEqualTo(10L);
        assertThat(version.getVersion()).isEqualTo(1);
        assertThat(version.getCreatedBy()).isEqualTo(42L);
        assertThat(version.getCreatedAt()).isNotNull();
        assertThat(version.getConfig()).isEqualTo(sampleConfig());
    }

    @Test
    void shouldReconstituteVersionWithExistingIdentity() {
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");

        JobVersion version = JobVersion.reconstitute(99L, 10L, 3, sampleConfig(), 42L, createdAt);

        assertThat(version.getId()).isEqualTo(99L);
        assertThat(version.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldThrowWhenJobIdOrConfigIsNull() {
        assertThatThrownBy(() -> JobVersion.create(null, 1, sampleConfig(), 42L))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("jobId must not be null");
        assertThatThrownBy(() -> JobVersion.create(10L, 1, null, 42L))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("config must not be null");
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1_000,
                java.util.List.of()
        );
    }
}
