package com.cromp.schedules.domain.repository;

import com.cromp.schedules.domain.model.Schedule;
import java.util.Optional;

public interface ScheduleRepositoryPort {
    Schedule save(Schedule schedule);
    Optional<Schedule> findByJobId(Long jobId);
    void delete(Schedule schedule);
}