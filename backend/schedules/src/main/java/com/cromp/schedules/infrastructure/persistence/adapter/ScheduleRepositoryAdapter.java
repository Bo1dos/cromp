package com.cromp.schedules.infrastructure.persistence.adapter;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import com.cromp.schedules.infrastructure.persistence.jpa.repository.ScheduleJpaRepository;
import com.cromp.schedules.infrastructure.persistence.mapper.SchedulePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class ScheduleRepositoryAdapter implements ScheduleRepositoryPort {

    private final ScheduleJpaRepository repository;
    private final SchedulePersistenceMapper mapper;

    @Override
    public Schedule save(Schedule schedule) {
        return mapper.toDomain(repository.save(mapper.toJpa(schedule)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Schedule> findByJobId(Long jobId) {
        return repository.findByJobId(jobId).map(mapper::toDomain);
    }

    @Override
    public void delete(Schedule schedule) {
        repository.delete(mapper.toJpa(schedule));
    }
}