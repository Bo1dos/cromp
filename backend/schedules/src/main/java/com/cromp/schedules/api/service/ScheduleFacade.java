package com.cromp.schedules.api.service;

import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;

public interface ScheduleFacade {
    ScheduleResponse createOrUpdate(Long organizationId, Long jobId, CreateUpdateScheduleRequest request);
    ScheduleResponse getSchedule(Long organizationId, Long jobId);
    void deleteSchedule(Long organizationId, Long jobId);
}