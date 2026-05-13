package com.cromp.orchestrator.agents.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentJpaRepository extends JpaRepository<AgentJpaEntity, Long> {
    Optional<AgentJpaEntity> findByAgentUuid(UUID agentUuid);
}
