package com.cromp.schedules.domain.model.exceptions;

public class InvalidCronExpressionException extends RuntimeException {
    public InvalidCronExpressionException(String expression, String reason) {
        super("Invalid cron expression: " + expression + ". " + reason);
    }
}