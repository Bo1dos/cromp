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
@Table(name = "users")
@DynamicInsert
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", nullable = false, unique = true, updatable = false)
    private UUID userUuid;

    @Column(name = "email", nullable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "last_name", length = SchemaLimits.USER_LAST_NAME_MAX_LENGTH)
    private String lastName;

    @Column(name = "first_name", length = SchemaLimits.USER_FIRST_NAME_MAX_LENGTH)
    private String firstName;

    @Column(name = "middle_name", length = SchemaLimits.USER_MIDDLE_NAME_MAX_LENGTH)
    private String middleName;

    @Column(name = "display_name", length = SchemaLimits.USER_DISPLAY_NAME_MAX_LENGTH)
    private String displayName;

    @Column(name = "password_hash", columnDefinition = "text")
    private String passwordHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    @Column(name = "profile", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> profile = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "is_deleted", insertable = false, updatable = false)
    private Boolean isDeleted;
}