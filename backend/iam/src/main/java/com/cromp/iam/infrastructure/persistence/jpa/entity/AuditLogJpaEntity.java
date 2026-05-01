package com.cromp.iam.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Immutable;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "audit_log")
@Immutable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "actor_id")
    private Long actorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "actor_snapshot", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> actorSnapshot = new HashMap<>();

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "changes_diff", columnDefinition = "jsonb")
    private Map<String, Object> changesDiff = new HashMap<>();
}