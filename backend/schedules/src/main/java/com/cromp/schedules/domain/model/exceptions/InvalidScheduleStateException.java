package com.cromp.schedules.domain.model.exceptions;

public class InvalidScheduleStateException extends RuntimeException {
    public InvalidScheduleStateException(String message) {
        super(message);
    }
}