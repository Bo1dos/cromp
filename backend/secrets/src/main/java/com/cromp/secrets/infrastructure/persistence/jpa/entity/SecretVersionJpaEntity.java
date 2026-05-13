package com.cromp.secrets.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "secret_versions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecretVersionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "secret_id", nullable = false)
    private Long secretId;

    @Column(nullable = false)
    private int version;

    @Column(name = "value_cipher", nullable = false, columnDefinition = "bytea")
    private byte[] valueCipher;

    @Column(name = "key_id", length = 255)
    private String keyId;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deprecated_at")
    private Instant deprecatedAt;
}