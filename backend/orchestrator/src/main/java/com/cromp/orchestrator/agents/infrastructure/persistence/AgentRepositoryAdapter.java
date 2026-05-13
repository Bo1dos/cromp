package com.cromp.orchestrator.agents.infrastructure.persistence;

import com.cromp.orchestrator.agents.domain.Agent;
import com.cromp.orchestrator.agents.domain.AgentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AgentRepositoryAdapter implements AgentRepositoryPort {
    private final AgentJpaRepository repository;
    private final AgentPersistenceMapper mapper;

    @Override
    public Agent save(Agent agent) {
        return mapper.toDomain(repository.save(mapper.toJpa(agent)));
    }

    @Override
    public Optional<Agent> findByUuid(UUID agentUuid) {
        return repository.findByAgentUuid(agentUuid).map(mapper::toDomain);
    }
}
