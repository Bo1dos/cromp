package com.cromp.jobs.domain.model.exceptions;

public class InvalidJobStateException extends RuntimeException {
    public InvalidJobStateException(String message) { super(message); }
}