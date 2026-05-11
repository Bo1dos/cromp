package com.cromp.jobs.api.dto.response;

import java.util.UUID;

public record TriggerResponse(
        UUID executionId,
        String message
) {}