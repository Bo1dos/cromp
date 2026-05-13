package com.cromp.orchestrator.agents.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Agent {
    @EqualsAndHashCode.Include
    private UUID agentUuid;
    private Long id;
    private Long organizationId;
    private String name;
    private List<String> capabilities;
    private Instant lastHeartbeat;
    private AgentStatus status;
    private int currentLoad;
    private int maxConcurrent;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    private Agent(Long id, UUID agentUuid, Long organizationId, String name, List<String> capabilities,
                  Instant lastHeartbeat, AgentStatus status, int currentLoad, int maxConcurrent,
                  Map<String, Object> metadata, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.agentUuid = agentUuid == null ? UUID.randomUUID() : agentUuid;
        this.organizationId = organizationId;
        this.name = name == null || name.isBlank() ? "agent-" + this.agentUuid : name.trim();
        this.capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        this.lastHeartbeat = lastHeartbeat;
        this.status = status == null ? AgentStatus.OFFLINE : status;
        this.currentLoad = Math.max(0, currentLoad);
        this.maxConcurrent = maxConcurrent <= 0 ? 10 : maxConcurrent;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Agent register(Long organizationId, String name, List<String> capabilities, int maxConcurrent) {
        Instant now = Instant.now();
        return new Agent(null, UUID.randomUUID(), organizationId, name, capabilities, now,
                AgentStatus.ONLINE, 0, maxConcurrent, Map.of(), now, now);
    }

    public static Agent reconstitute(Long id, UUID agentUuid, Long organizationId, String name,
                                     List<String> capabilities, Instant lastHeartbeat, AgentStatus status,
                                     int currentLoad, int maxConcurrent, Map<String, Object> metadata,
                                     Instant createdAt, Instant updatedAt) {
        return new Agent(id, agentUuid, organizationId, name, capabilities, lastHeartbeat, status,
                currentLoad, maxConcurrent, metadata, createdAt, updatedAt);
    }

    public void heartbeat(int currentLoad, Map<String, Object> metadata) {
        this.lastHeartbeat = Instant.now();
        this.status = AgentStatus.ONLINE;
        this.currentLoad = Math.max(0, currentLoad);
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        this.updatedAt = this.lastHeartbeat;
    }
}
