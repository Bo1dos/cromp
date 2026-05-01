package com.cromp.iam.domain.model;

import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.domain.model.exceptions.DomainException;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static com.cromp.iam.domain.model.support.DomainChecks.normalizeEmail;
import static com.cromp.iam.domain.model.support.DomainChecks.requireNonNullValue;
import static com.cromp.iam.domain.model.support.DomainChecks.requireText;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Invitation {

    private Long id;

    @EqualsAndHashCode.Include
    private Long organizationId;

    private String email;
    private String tokenHash;
    private Long roleId;
    private Long invitedBy;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant acceptedAt;
    private InvitationStatus status;

    private Invitation(Long id,
                       Long organizationId,
                       String email,
                       String tokenHash,
                       Long roleId,
                       Long invitedBy,
                       Instant expiresAt,
                       Instant createdAt,
                       Instant acceptedAt,
                       InvitationStatus status) {
        this.id = id;
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        this.email = normalizeEmail(email);
        this.tokenHash = requireText(tokenHash, "tokenHash");
        this.roleId = requireNonNullValue(roleId, "roleId");
        this.invitedBy = invitedBy;
        this.expiresAt = requireNonNullValue(expiresAt, "expiresAt");
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.acceptedAt = acceptedAt;
        this.status = requireNonNullValue(status, "status");
    }

    public static Invitation create(Long organizationId,
                                    String email,
                                    String tokenHash,
                                    Long roleId,
                                    Long invitedBy,
                                    Instant expiresAt) {
        return new Invitation(
                null,
                organizationId,
                email,
                tokenHash,
                roleId,
                invitedBy,
                expiresAt,
                Instant.now(),
                null,
                InvitationStatus.PENDING
        );
    }

    public static Invitation reconstitute(Long id,
                                          Long organizationId,
                                          String email,
                                          String tokenHash,
                                          Long roleId,
                                          Long invitedBy,
                                          Instant expiresAt,
                                          Instant createdAt,
                                          Instant acceptedAt,
                                          InvitationStatus status) {
        return new Invitation(
                id,
                organizationId,
                email,
                tokenHash,
                roleId,
                invitedBy,
                expiresAt,
                createdAt,
                acceptedAt,
                status
        );
    }

    public void accept() {
        ensurePending();
        if (Instant.now().isAfter(expiresAt)) {
            this.status = InvitationStatus.EXPIRED;
            throw new DomainException("Invitation is expired");
        }
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    public void revoke() {
        ensureNotAccepted();
        this.status = InvitationStatus.REVOKED;
    }

    public void expire() {
        ensureNotAccepted();
        this.status = InvitationStatus.EXPIRED;
    }

    public void validate() {
        requireNonNullValue(organizationId, "organizationId");
        requireText(email, "email");
        requireText(tokenHash, "tokenHash");
        requireNonNullValue(roleId, "roleId");
        requireNonNullValue(expiresAt, "expiresAt");
        requireNonNullValue(status, "status");
    }

    private void ensurePending() {
        if (status != InvitationStatus.PENDING) {
            throw new DomainException("Only pending invitation can be accepted");
        }
    }

    private void ensureNotAccepted() {
        if (status == InvitationStatus.ACCEPTED) {
            throw new DomainException("Accepted invitation cannot be changed");
        }
    }
}