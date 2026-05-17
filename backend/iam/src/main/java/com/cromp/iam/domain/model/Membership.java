package com.cromp.iam.domain.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.cromp.iam.domain.model.support.DomainChecks.requireNonNullValue;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Membership extends AbstractAuditableDomainEntity {

    @EqualsAndHashCode.Include
    private UUID membershipUuid;

    private Long userId;
    private Long organizationId;
    private Long roleId;

    private Membership(Long id,
                       Instant createdAt,
                       Instant updatedAt,
                       Instant deletedAt,
                       UUID membershipUuid,
                       Long userId,
                       Long organizationId,
                       Long roleId) {
        super(id, createdAt, updatedAt, null);
        this.membershipUuid = membershipUuid == null ? UUID.randomUUID() : membershipUuid;
        this.userId = requireNonNullValue(userId, "userId");
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        this.roleId = requireNonNullValue(roleId, "roleId");
    }

    public static Membership join(Long userId, Long organizationId, Long roleId) {
        return new Membership(null, Instant.now(), Instant.now(), null,
                UUID.randomUUID(), userId, organizationId, roleId);
    }

    public static Membership reconstitute(Long id,
                                          UUID membershipUuid,
                                          Long userId,
                                          Long organizationId,
                                          Long roleId,
                                          Instant createdAt,
                                          Instant updatedAt,
                                          Instant deletedAt) {
        return new Membership(id, createdAt, updatedAt, deletedAt,
                membershipUuid, userId, organizationId, roleId);
    }

    public void changeRole(Long roleId) {
        this.roleId = requireNonNullValue(roleId, "roleId");
        touch();
    }

    public void validate() {
        requireNonNullValue(membershipUuid, "membershipUuid");
        requireNonNullValue(userId, "userId");
        requireNonNullValue(organizationId, "organizationId");
        requireNonNullValue(roleId, "roleId");
    }
}