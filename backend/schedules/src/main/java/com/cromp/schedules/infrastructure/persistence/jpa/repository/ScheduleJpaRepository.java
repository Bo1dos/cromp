package com.cromp.schedules.infrastructure.persistence.jpa.repository;

import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.infrastructure.persistence.jpa.entity.ScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleJpaRepository extends JpaRepository<ScheduleJpaEntity, Long> {
    Optional<ScheduleJpaEntity> findByJobId(Long jobId);
    List<ScheduleJpaEntity> findAllByStatus(ScheduleStatus status);
}
