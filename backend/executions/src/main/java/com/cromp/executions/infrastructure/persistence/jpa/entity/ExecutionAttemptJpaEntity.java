package com.cromp.executions.infrastructure.persistence.jpa.entity;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "execution_attempts")
@DynamicInsert
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionAttemptJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_uuid", nullable = false, unique = true, updatable = false)
    private UUID attemptUuid;

    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "attempt_status")
    private AttemptStatus status;

    @Column(name = "status_reason", columnDefinition = "TEXT")
    private String statusReason;

    @Column(name = "error_class", columnDefinition = "TEXT")
    private String errorClass;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "executor_metadata", columnDefinition = "jsonb")
    private Map<String, Object> executorMetadata = new HashMap<>();

    @Column(name = "idempotency_key")
    private UUID idempotencyKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "output_summary", columnDefinition = "jsonb")
    private Map<String, Object> outputSummary = new HashMap<>();

    @Column(name = "trace_id")
    private UUID traceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
