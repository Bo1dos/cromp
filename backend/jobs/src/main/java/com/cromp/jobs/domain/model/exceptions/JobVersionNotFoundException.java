package com.cromp.jobs.domain.model.exceptions;

public class JobVersionNotFoundException extends RuntimeException {
    public JobVersionNotFoundException(String message) { super(message); }
    public JobVersionNotFoundException(Long jobId, int version) {
        super("Version " + version + " not found for job " + jobId);
    }
}