package com.cromp.orchestrator.agents.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentTest {
    @Test
    void registerCreatesOnlineAgentAndHeartbeatUpdatesLoad() {
        Agent agent = Agent.register(10L, "worker-1", List.of("HTTP"), 2);

        assertThat(agent.getStatus()).isEqualTo(AgentStatus.ONLINE);
        assertThat(agent.getCapabilities()).containsExactly("HTTP");

        agent.heartbeat(1, Map.of("region", "local"));

        assertThat(agent.getCurrentLoad()).isEqualTo(1);
        assertThat(agent.getMetadata()).containsEntry("region", "local");
    }
}
