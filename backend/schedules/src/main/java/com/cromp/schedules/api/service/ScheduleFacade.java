package com.cromp.schedules.api.service;

import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;

import java.util.UUID;

public interface ScheduleFacade {
    ScheduleResponse createOrUpdate(UUID jobUuid, CreateUpdateScheduleRequest request);
    ScheduleResponse getSchedule(UUID jobUuid);
    void deleteSchedule(UUID jobUuid);
}