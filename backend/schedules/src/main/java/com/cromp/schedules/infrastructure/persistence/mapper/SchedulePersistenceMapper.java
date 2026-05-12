package com.cromp.schedules.infrastructure.persistence.mapper;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.infrastructure.persistence.jpa.entity.ScheduleJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SchedulePersistenceMapper {

    public ScheduleJpaEntity toJpa(Schedule schedule) {
        return ScheduleJpaEntity.builder()
                .id(schedule.getId())
                .jobId(schedule.getJobId())
                .cronExpression(schedule.getCronExpression())
                .timezone(schedule.getTimezone())
                .rules(schedule.getRules())
                .nextRunAt(schedule.getNextRunAt())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    public Schedule toDomain(ScheduleJpaEntity entity) {
        return Schedule.reconstitute(
                entity.getId(),
                entity.getJobId(),
                entity.getCronExpression(),
                entity.getTimezone(),
                entity.getRules(),
                entity.getNextRunAt(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                null  // deletedAt не используется
        );
    }
}