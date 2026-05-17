package com.cromp.iam.infrastructure.persistence.jpa.entity;

import com.cromp.iam.domain.model.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitation_uuid", nullable = false, unique = true, updatable = false)
    private UUID invitationUuid;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "email", nullable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "invited_by")
    private Long invitedBy;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "invitation_status")
    private InvitationStatus status;
}