package com.cromp.jobs.infrastructure.persistence.adapter;

import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.infrastructure.persistence.jpa.repository.JobVersionJpaRepository;
import com.cromp.jobs.infrastructure.persistence.mapper.JobVersionPersistenceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobVersionRepositoryAdapterTest {

    @Mock private JobVersionJpaRepository repository;

    @Test
    void shouldSaveAndRetrieveLatestVersion() {
        JobVersionPersistenceMapper mapper = new JobVersionPersistenceMapper(new ObjectMapper());
        JobVersionRepositoryAdapter adapter = new JobVersionRepositoryAdapter(repository, mapper);
        JobVersion version = JobVersion.reconstitute(1L, 10L, 2, sampleConfig(), 42L,
                Instant.parse("2024-01-01T00:00:00Z"));

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findTopByJobIdOrderByVersionDesc(10L)).thenReturn(Optional.empty());
        when(repository.findByJobIdOrderByVersionDesc(10L)).thenReturn(List.of());

        assertThat(adapter.save(version)).isEqualTo(version);
        assertThat(adapter.findLatestByJobId(10L)).isEmpty();
        assertThat(adapter.findByJobIdOrderByVersionDesc(10L)).isEmpty();
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", java.util.Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1_000,
                java.util.List.of()
        );
    }
}
