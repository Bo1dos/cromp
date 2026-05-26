package com.cromp.executions.infrastructure.port;

import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.application.port.AttemptClaimPort;
import com.cromp.executions.application.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttemptClaimPortImpl implements AttemptClaimPort {

    private final AttemptService attemptService;

    //TODO: убедиться, что не падает на null
    @Override
    public Optional<ClaimAttemptResult> claimNextAttempt(Long organizationId) {
        return attemptService.claimNextAttempt(organizationId);
    }
}