package com.cromp.jobs.api.mapper;

import com.cromp.jobs.api.dto.response.JobResponse;
import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.domain.model.enums.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobApiMapperTest {

    private final JobApiMapper mapper = new JobApiMapper();

    @Test
    void shouldMapJobAndCurrentVersionToResponse() {
        Job job = Job.reconstitute(1L, UUID.randomUUID(), 11L, "Daily sync", "desc", JobStatus.ACTIVE,
                "default", 5, 42L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"), null);
        JobVersion version = JobVersion.create(1L, 2, sampleConfig(), 42L);

        JobResponse response = mapper.toJobResponse(job, version, 2);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.jobUuid()).isEqualTo(job.getJobUuid());
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.currentVersion()).isEqualTo(2);
        assertThat(response.versionCount()).isEqualTo(2);
        assertThat(response.currentConfig()).isNotNull();
        assertThat(response.currentConfig().target().headers()).containsEntry("X-Trace", "1");
    }

    @Test
    void shouldMapNullVersionToResponseWithoutConfig() {
        Job job = Job.reconstitute(1L, UUID.randomUUID(), 11L, "Daily sync", null, JobStatus.ACTIVE,
                "default", 5, 42L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"), null);

        JobResponse response = mapper.toJobResponse(job, null, 0);

        assertThat(response.currentConfig()).isNull();
        assertThat(response.currentVersion()).isZero();
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of("X-Trace", "1"), "payload"),
                RetryPolicy.defaultPolicy(),
                1_000,
                List.of(new JobSecretRef(UUID.randomUUID(), "API_KEY"))
        );
    }
}
