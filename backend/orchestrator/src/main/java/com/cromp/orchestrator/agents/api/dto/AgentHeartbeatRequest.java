package com.cromp.orchestrator.agents.api.dto;

import java.util.Map;

public record AgentHeartbeatRequest(Integer currentLoad, Map<String, Object> metadata) {}
