package com.cromp.schedules.infrastructure.port;

import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobOwnershipPortImplTest {

    @Mock
    private JobRepositoryPort jobRepository;

    @Test
    void jobBelongsToOrganizationShouldReturnTrueWhenJobExistsInOrganization() {
        JobOwnershipPortImpl port = new JobOwnershipPortImpl(jobRepository);
        when(jobRepository.findByIdAndOrganizationId(10L, 20L)).thenReturn(Optional.of(job(10L, 20L)));

        assertThat(port.jobBelongsToOrganization(10L, 20L)).isTrue();
    }

    @Test
    void jobBelongsToOrganizationShouldReturnFalseWhenJobIsMissingOrForeign() {
        JobOwnershipPortImpl port = new JobOwnershipPortImpl(jobRepository);
        when(jobRepository.findByIdAndOrganizationId(10L, 20L)).thenReturn(Optional.empty());

        assertThat(port.jobBelongsToOrganization(10L, 20L)).isFalse();
    }

    private static Job job(Long id, Long organizationId) {
        return Job.reconstitute(id, UUID.randomUUID(), organizationId, "Daily sync", null, JobStatus.ACTIVE,
                "default", 1, 11L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"), null);
    }
}
