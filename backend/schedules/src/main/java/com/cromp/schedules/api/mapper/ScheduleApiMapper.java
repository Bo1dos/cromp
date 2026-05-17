package com.cromp.schedules.api.mapper;

import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.domain.model.Schedule;
import org.springframework.stereotype.Component;

@Component
public class ScheduleApiMapper {

    public ScheduleResponse toResponse(Schedule schedule) {
        return new ScheduleResponse(
                schedule.getCronExpression(),
                schedule.getTimezone(),
                schedule.getRules(),
                schedule.getNextRunAt(),
                schedule.getStatus().name(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}