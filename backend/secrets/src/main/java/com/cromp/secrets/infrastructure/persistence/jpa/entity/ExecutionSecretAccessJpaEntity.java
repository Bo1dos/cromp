package com.cromp.secrets.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;

@Entity
@Table(name = "execution_secret_access")
@DynamicInsert
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionSecretAccessJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(name = "secret_version_id", nullable = false)
    private Long secretVersionId;

    @Column(name = "accessed_key", nullable = false)
    private String accessedKey;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;
}
