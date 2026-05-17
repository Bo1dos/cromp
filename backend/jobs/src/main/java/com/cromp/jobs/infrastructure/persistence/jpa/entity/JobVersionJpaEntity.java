package com.cromp.jobs.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(
        name = "job_versions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_job_versions_job_version",
                columnNames = {"job_id", "version"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobVersionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "version", nullable = false)
    private int version;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion;

    @Column(name = "config", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String config; // будем хранить сериализованный JobConfig в JSON строку

    @Column(name = "target_url", columnDefinition = "TEXT")
    private String targetUrl;    // генерируется триггером, можно хранить для запросов

    @Column(length = 10)
    private String method;

    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    @Column(name = "retry_policy", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String retryPolicy;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}