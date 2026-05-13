package com.cromp.orchestrator.agents.infrastructure.persistence;

import com.cromp.orchestrator.agents.domain.AgentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "agents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_uuid", nullable = false, unique = true, updatable = false)
    private UUID agentUuid;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(columnDefinition = "jsonb")
    private List<String> capabilities = new ArrayList<>();

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status;

    @Column(name = "current_load")
    private int currentLoad;

    @Column(name = "max_concurrent")
    private int maxConcurrent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
