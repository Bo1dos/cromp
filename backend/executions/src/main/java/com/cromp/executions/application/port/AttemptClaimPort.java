package com.cromp.executions.application.port;

import com.cromp.executions.api.dto.response.ClaimAttemptResult;

import java.util.Optional;

// Вызывается executor'ом: захватить следующую PENDING попытку
public interface AttemptClaimPort {
    Optional<ClaimAttemptResult> claimNextAttempt(Long organizationId);
}