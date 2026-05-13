package com.cromp.orchestrator.agents.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RegisterAgentRequest(
        @NotNull Long organizationId,
        @NotBlank String agentToken,
        String name,
        List<String> capabilities,
        Integer maxConcurrent
) {}
