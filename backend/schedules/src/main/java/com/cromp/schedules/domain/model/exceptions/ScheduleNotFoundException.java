package com.cromp.schedules.domain.model.exceptions;

public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(String message) {
        super(message);
    }
    public ScheduleNotFoundException(Long jobId) {
        super("Schedule not found for job id=" + jobId);
    }
}