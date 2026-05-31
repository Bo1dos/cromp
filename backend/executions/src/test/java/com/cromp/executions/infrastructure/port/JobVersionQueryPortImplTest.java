package com.cromp.executions.infrastructure.port;

import com.cromp.jobs.domain.model.JobVersion;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobTarget;
import com.cromp.jobs.domain.model.RetryPolicy;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobVersionQueryPortImplTest {

    @Mock private JobVersionRepositoryPort jobVersionRepository;

    @Test
    void shouldSerializeJobConfigToJsonWhenVersionExists() {
        ObjectMapper objectMapper = new ObjectMapper();
        JobVersionQueryPortImpl port = new JobVersionQueryPortImpl(jobVersionRepository, objectMapper);
        JobVersion version = JobVersion.reconstitute(1L, 10L, 1, sampleConfig(), 11L, Instant.parse("2024-01-01T00:00:00Z"));
        when(jobVersionRepository.findById(1L)).thenReturn(Optional.of(version));

        assertThat(port.findConfigJsonByVersionId(1L))
                .hasValueSatisfying(json -> assertThat(json).contains("\"timeoutMs\":1000"));
    }

    @Test
    void shouldReturnEmptyWhenVersionDoesNotExist() {
        JobVersionQueryPortImpl port = new JobVersionQueryPortImpl(jobVersionRepository, new ObjectMapper());
        when(jobVersionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(port.findConfigJsonByVersionId(1L)).isEmpty();
    }

    @Test
    void shouldWrapSerializationErrors() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
                throw new com.fasterxml.jackson.core.JsonProcessingException("boom") {};
            }
        };
        JobVersionQueryPortImpl port = new JobVersionQueryPortImpl(jobVersionRepository, objectMapper);
        JobVersion version = JobVersion.reconstitute(1L, 10L, 1, sampleConfig(), 11L, Instant.parse("2024-01-01T00:00:00Z"));
        when(jobVersionRepository.findById(1L)).thenReturn(Optional.of(version));

        assertThatThrownBy(() -> port.findConfigJsonByVersionId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize JobConfig");
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1000,
                List.of()
        );
    }
}
