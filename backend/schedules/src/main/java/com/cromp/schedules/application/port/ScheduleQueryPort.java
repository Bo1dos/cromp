package com.cromp.schedules.application.port;

import java.util.List;

import com.cromp.schedules.domain.model.Schedule;

public interface ScheduleQueryPort {
    List<Schedule> findActiveSchedulesReadyForRun();
}