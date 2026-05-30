package com.cromp.jobs.infrastructure.persistence.adapter;

import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.infrastructure.persistence.jpa.repository.JobJpaRepository;
import com.cromp.jobs.infrastructure.persistence.mapper.JobPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobRepositoryAdapterTest {

    @Mock private JobJpaRepository repository;
    @Mock private JobPersistenceMapper mapper;

    @Test
    void shouldSaveAndMapJob() {
        Job input = Job.create(UUID.randomUUID(), 11L, "Daily sync", null, "default", 1, 42L);
        Job saved = Job.reconstitute(1L, input.getJobUuid(), 11L, "Daily sync", null, JobStatus.ACTIVE,
                "default", 1, 42L, input.getCreatedAt(), input.getUpdatedAt(), null);
        JobRepositoryAdapter adapter = new JobRepositoryAdapter(repository, mapper);
        when(mapper.toJpa(input)).thenReturn(null);
        when(mapper.toDomain(isNull())).thenReturn(saved);

        assertThat(adapter.save(input)).isEqualTo(saved);
        verify(mapper).toJpa(input);
        verify(repository).save(null);
        verify(mapper).toDomain(null);
    }

    @Test
    void shouldDelegateFindsUsingSoftDeleteAwareQueries() {
        JobRepositoryAdapter adapter = new JobRepositoryAdapter(repository, mapper);
        when(repository.findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(11L)).thenReturn(java.util.List.of());
        when(repository.findByOrganizationIdAndStatusAndDeletedAtIsNull(11L, JobStatus.ACTIVE)).thenReturn(java.util.List.of());
        when(repository.existsByNameAndOrganizationIdAndDeletedAtIsNull("job", 11L)).thenReturn(true);

        assertThat(adapter.existsByNameAndOrganizationId("job", 11L)).isTrue();
        assertThat(adapter.findByOrganizationId(11L)).isEmpty();
        assertThat(adapter.findByOrganizationIdAndStatus(11L, JobStatus.ACTIVE)).isEmpty();
    }
}
