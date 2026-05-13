package com.cromp.orchestrator.agents.infrastructure.persistence;

import com.cromp.orchestrator.agents.domain.Agent;
import org.springframework.stereotype.Component;

@Component
public class AgentPersistenceMapper {
    public AgentJpaEntity toJpa(Agent agent) {
        return AgentJpaEntity.builder()
                .id(agent.getId())
                .agentUuid(agent.getAgentUuid())
                .organizationId(agent.getOrganizationId())
                .name(agent.getName())
                .capabilities(agent.getCapabilities())
                .lastHeartbeat(agent.getLastHeartbeat())
                .status(agent.getStatus())
                .currentLoad(agent.getCurrentLoad())
                .maxConcurrent(agent.getMaxConcurrent())
                .metadata(agent.getMetadata())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }

    public Agent toDomain(AgentJpaEntity entity) {
        return Agent.reconstitute(entity.getId(), entity.getAgentUuid(), entity.getOrganizationId(),
                entity.getName(), entity.getCapabilities(), entity.getLastHeartbeat(), entity.getStatus(),
                entity.getCurrentLoad(), entity.getMaxConcurrent(), entity.getMetadata(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
