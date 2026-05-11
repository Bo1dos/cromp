package com.cromp.jobs.domain.model.support;

public final class SchemaLimits {
    private SchemaLimits() {}

    public static final int JOB_NAME_MAX_LENGTH = 255;
    public static final int QUEUE_NAME_MAX_LENGTH = 100;
    public static final int TARGET_URL_MAX_LENGTH = 2048;
    public static final int METHOD_MAX_LENGTH = 10;
    public static final int ENV_NAME_MAX_LENGTH = 100;

    public static final int DEFAULT_MAX_ATTEMPTS = 3;
    public static final long DEFAULT_BACKOFF_MS = 1000;
    public static final double DEFAULT_BACKOFF_MULTIPLIER = 2.0;
}