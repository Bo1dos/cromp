package com.cromp.orchestrator.agents.infrastructure.web;

import com.cromp.orchestrator.agents.api.dto.AgentHeartbeatRequest;
import com.cromp.orchestrator.agents.api.dto.AgentResponse;
import com.cromp.orchestrator.agents.api.dto.RegisterAgentRequest;
import com.cromp.orchestrator.agents.application.AgentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/agents")
@RequiredArgsConstructor
public class AgentController {
    private final AgentApplicationService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse register(@Valid @RequestBody RegisterAgentRequest request) {
        return service.register(request);
    }

    @PostMapping("/{agentId}/heartbeat")
    public AgentResponse heartbeat(@PathVariable UUID agentId, @RequestBody AgentHeartbeatRequest request) {
        return service.heartbeat(agentId, request);
    }
}
