package com.cromp.schedules.infrastructure.persistence.adapter;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.infrastructure.persistence.jpa.entity.ScheduleJpaEntity;
import com.cromp.schedules.infrastructure.persistence.jpa.repository.ScheduleJpaRepository;
import com.cromp.schedules.infrastructure.persistence.mapper.SchedulePersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleRepositoryAdapterTest {

    @Mock
    private ScheduleJpaRepository repository;
    @Mock
    private SchedulePersistenceMapper mapper;

    @Test
    void saveShouldMapToJpaPersistAndMapBackToDomain() {
        Schedule schedule = schedule();
        ScheduleJpaEntity entity = ScheduleJpaEntity.builder().id(99L).build();
        Schedule saved = Schedule.reconstitute(99L, 10L, "*/5 * * * *", "UTC", null,
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"), null);

        when(mapper.toJpa(schedule)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);

        ScheduleRepositoryAdapter adapter = new ScheduleRepositoryAdapter(repository, mapper);

        assertThat(adapter.save(schedule)).isEqualTo(saved);
        verify(mapper).toJpa(schedule);
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByJobIdShouldMapRepositoryResult() {
        ScheduleJpaEntity entity = ScheduleJpaEntity.builder().id(1L).jobId(10L).build();
        Schedule domain = schedule();
        when(repository.findByJobId(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        ScheduleRepositoryAdapter adapter = new ScheduleRepositoryAdapter(repository, mapper);

        assertThat(adapter.findByJobId(10L)).contains(domain);
        verify(repository).findByJobId(10L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findByJobIdShouldReturnEmptyWhenNotFound() {
        when(repository.findByJobId(999L)).thenReturn(Optional.empty());

        ScheduleRepositoryAdapter adapter = new ScheduleRepositoryAdapter(repository, mapper);

        assertThat(adapter.findByJobId(999L)).isEmpty();
        verify(repository).findByJobId(999L);
        verifyNoInteractions(mapper);
    }

    @Test
    void findAllByStatusShouldMapEveryEntityToDomain() {
        ScheduleJpaEntity entity = ScheduleJpaEntity.builder().id(1L).status(ScheduleStatus.ACTIVE).build();
        Schedule domain = schedule();
        when(repository.findAllByStatus(ScheduleStatus.ACTIVE)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        ScheduleRepositoryAdapter adapter = new ScheduleRepositoryAdapter(repository, mapper);

        assertThat(adapter.findAllByStatus(ScheduleStatus.ACTIVE)).containsExactly(domain);
    }

    @Test
    void deleteShouldMapDomainToJpaAndDelegateDelete() {
        Schedule schedule = schedule();
        ScheduleJpaEntity entity = ScheduleJpaEntity.builder().id(1L).build();
        when(mapper.toJpa(schedule)).thenReturn(entity);

        ScheduleRepositoryAdapter adapter = new ScheduleRepositoryAdapter(repository, mapper);
        adapter.delete(schedule);

        verify(mapper).toJpa(schedule);
        verify(repository).delete(entity);
    }

    private static Schedule schedule() {
        return Schedule.reconstitute(1L, 10L, "*/5 * * * *", "UTC", null,
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"), null);
    }
}
