package com.cromp.iam.infrastructure.persistence.jpa.entity;

import com.cromp.iam.domain.model.support.SchemaLimits;
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
@Table(name = "organizations")
@DynamicInsert
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_uuid", nullable = false, unique = true, updatable = false)
    private UUID orgUuid;

    @Column(name = "name", nullable = false, length = SchemaLimits.ORG_NAME_MAX_LENGTH)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "settings", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> settings = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "is_deleted", insertable = false, updatable = false)
    private Boolean isDeleted;
}