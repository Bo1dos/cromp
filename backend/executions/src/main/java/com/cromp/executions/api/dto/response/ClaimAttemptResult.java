package com.cromp.executions.api.dto.response;

import java.util.UUID;

/*Возвращается orchestrator'у при захвате попытки*/
public record ClaimAttemptResult(
        Long attemptId,
        UUID attemptUuid,
        Long executionId,
        Long organizationId,
        Long jobId,
        String jobConfig   // JSONB конфиг версии — исполнитель сам десериализует
) {}
