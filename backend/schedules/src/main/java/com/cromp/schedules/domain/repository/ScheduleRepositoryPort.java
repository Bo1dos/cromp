package com.cromp.schedules.domain.repository;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;

import java.util.List;
import java.util.Optional;

/**
 * Доменный порт репозитория расписаний.
 *
 * Расширен методом {@code findAllByStatus} для нужд orchestrator'а:
 * планировщику нужно читать все ACTIVE расписания без привязки к конкретному jobId.
 */
public interface ScheduleRepositoryPort {
 
    Schedule save(Schedule schedule);
 
    Optional<Schedule> findByJobId(Long jobId);
 
    void delete(Schedule schedule);
 
    /**
     * Возвращает все расписания с указанным статусом.
     * Используется orchestrator'ом для выборки ACTIVE расписаний.
     */
    List<Schedule> findAllByStatus(ScheduleStatus status);
}
