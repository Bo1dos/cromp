package com.cromp.orchestrator.agents.domain;

import java.util.Optional;
import java.util.UUID;

public interface AgentRepositoryPort {
    Agent save(Agent agent);
    Optional<Agent> findByUuid(UUID agentUuid);
}
