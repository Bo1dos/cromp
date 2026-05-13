package com.cromp.secrets.domain.model;

import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.support.AbstractAuditableDomainEntity;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.cromp.secrets.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Secret extends AbstractAuditableDomainEntity {
    private static final int NAME_MAX_LENGTH = 255;

    @EqualsAndHashCode.Include
    private UUID secretUuid;
    private Long organizationId;
    private String name;
    private SecretScope scope;
    private String description;

    private Secret(Long id, UUID secretUuid, Long organizationId, String name, SecretScope scope,
                   String description, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        super(id, createdAt, updatedAt, deletedAt);
        this.secretUuid = secretUuid == null ? UUID.randomUUID() : secretUuid;
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        this.name = requireMaxLength(requireText(name, "name"), NAME_MAX_LENGTH, "name");
        this.scope = requireNonNullValue(scope, "scope");
        this.description = description != null ? description.trim() : null;
    }

    public static Secret create(UUID secretUuid, Long organizationId, String name, SecretScope scope, String description) {
        Instant now = Instant.now();
        return new Secret(null, secretUuid, organizationId, name, scope, description, now, now, null);
    }

    public static Secret reconstitute(Long id, UUID secretUuid, Long organizationId, String name,
                                      SecretScope scope, String description, Instant createdAt,
                                      Instant updatedAt, Instant deletedAt) {
        return new Secret(id, secretUuid, organizationId, name, scope, description, createdAt, updatedAt, deletedAt);
    }

    public void rename(String name) {
        ensureActive();
        this.name = requireMaxLength(requireText(name, "name"), NAME_MAX_LENGTH, "name");
        touch();
    }

    public void changeDescription(String description) {
        ensureActive();
        this.description = description != null ? description.trim() : null;
        touch();
    }

    public void changeScope(SecretScope scope) {
        ensureActive();
        this.scope = requireNonNullValue(scope, "scope");
        touch();
    }

    public void delete() {
        markDeleted();
    }

    private void ensureActive() {
        if (isDeleted()) {
            throw new IllegalStateException("Cannot modify deleted secret");
        }
    }
}
