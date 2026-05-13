package com.cromp.orchestrator.executor;

import java.util.Map;
import java.util.UUID;

public record ClaimedAttempt(
        Long attemptId,
        Long executionId,
        UUID attemptUuid,
        Map<String, Object> config
) {}
