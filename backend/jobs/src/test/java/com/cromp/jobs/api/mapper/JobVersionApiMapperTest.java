package com.cromp.jobs.api.mapper;

import com.cromp.jobs.api.dto.response.JobVersionResponse;
import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.domain.model.enums.JobStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobVersionApiMapperTest {

    private final JobVersionApiMapper mapper = new JobVersionApiMapper();

    @Test
    void shouldMapVersionToResponse() {
        Job job = Job.reconstitute(1L, UUID.randomUUID(), 11L, "Daily sync", null, JobStatus.ACTIVE,
                "default", 5, 42L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"), null);
        JobVersion version = JobVersion.reconstitute(7L, 1L, 3, sampleConfig(), 42L, Instant.parse("2024-01-02T00:00:00Z"));

        JobVersionResponse response = mapper.toVersionResponse(job, version);

        assertThat(response.jobUuid()).isEqualTo(job.getJobUuid());
        assertThat(response.version()).isEqualTo(3);
        assertThat(response.createdAt()).isEqualTo(version.getCreatedAt());
        assertThat(response.config().retryPolicy().maxAttempts()).isEqualTo(3);
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1_000,
                List.of()
        );
    }
}
