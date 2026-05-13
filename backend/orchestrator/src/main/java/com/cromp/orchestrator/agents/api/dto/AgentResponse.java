package com.cromp.orchestrator.agents.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AgentResponse(
        Long id,
        UUID agentUuid,
        Long organizationId,
        String name,
        List<String> capabilities,
        String status,
        int currentLoad,
        int maxConcurrent,
        Map<String, Object> metadata,
        Instant lastHeartbeat
) {}
