package com.cromp.orchestrator.agents.application;

import com.cromp.orchestrator.agents.api.dto.AgentHeartbeatRequest;
import com.cromp.orchestrator.agents.api.dto.AgentResponse;
import com.cromp.orchestrator.agents.api.dto.RegisterAgentRequest;
import com.cromp.orchestrator.agents.domain.Agent;
import com.cromp.orchestrator.agents.domain.AgentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentApplicationService {
    private final AgentRepositoryPort repository;

    @Value("${cromp.orchestrator.agents.registration-token}")
    private String registrationToken;

    public AgentResponse register(RegisterAgentRequest request) {
        if (!registrationToken.equals(request.agentToken())) {
            throw new SecurityException("Invalid agent token");
        }
        Agent agent = repository.save(Agent.register(request.organizationId(), request.name(),
                request.capabilities(), request.maxConcurrent() != null ? request.maxConcurrent() : 10));
        return toResponse(agent);
    }

    public AgentResponse heartbeat(UUID agentId, AgentHeartbeatRequest request) {
        Agent agent = repository.findByUuid(agentId).orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        agent.heartbeat(request.currentLoad() != null ? request.currentLoad() : 0, request.metadata());
        return toResponse(repository.save(agent));
    }

    private AgentResponse toResponse(Agent agent) {
        return new AgentResponse(agent.getId(), agent.getAgentUuid(), agent.getOrganizationId(), agent.getName(),
                agent.getCapabilities(), agent.getStatus().name(), agent.getCurrentLoad(), agent.getMaxConcurrent(),
                agent.getMetadata(), agent.getLastHeartbeat());
    }
}
