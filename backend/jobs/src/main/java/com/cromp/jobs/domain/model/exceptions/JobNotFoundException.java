package com.cromp.jobs.domain.model.exceptions;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String message) { super(message); }
    public JobNotFoundException(Long jobId) { super("Job not found: id=" + jobId); }
}