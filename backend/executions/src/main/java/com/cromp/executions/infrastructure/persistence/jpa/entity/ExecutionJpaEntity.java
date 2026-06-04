package com.cromp.executions.infrastructure.persistence.jpa.entity;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "executions")
@DynamicInsert
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exec_uuid", nullable = false, unique = true, updatable = false)
    private UUID execUuid;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_version_id")
    private Long jobVersionId;

    @Column(name = "priority")
    private int priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private ExecutionSource source;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "final_status", nullable = false, columnDefinition = "execution_status")
    private ExecutionStatus finalStatus;

    @Column(name = "total_attempts", nullable = false)
    private int totalAttempts;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Column(name = "execution_policy_snapshot", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String executionPolicySnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}