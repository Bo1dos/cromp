package com.cromp.secrets.domain.model;

import com.cromp.secrets.domain.model.enums.SecretScope;
import com.cromp.secrets.domain.model.support.AbstractAuditableDomainEntity;
import com.cromp.secrets.domain.model.support.DomainChecks;
import com.cromp.secrets.domain.model.support.SchemaLimits;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Secret extends AbstractAuditableDomainEntity {
    private UUID secretUuid;
    private Long organizationId;
    private String name;
    private SecretScope scope;
    private String description;

    private Secret(Long id, Instant createdAt, Instant updatedAt, Instant deletedAt,
                   UUID secretUuid, Long organizationId, String name,
                   SecretScope scope, String description) {
        super(id, createdAt, updatedAt, deletedAt);
        this.secretUuid = secretUuid == null ? UUID.randomUUID() : secretUuid;
        this.organizationId = DomainChecks.requireNonNullValue(organizationId, "organizationId");
        this.name = DomainChecks.requireMaxLength(
                DomainChecks.requireText(name, "name"),
                SchemaLimits.SECRET_NAME_MAX_LENGTH, "name");
        this.scope = DomainChecks.requireNonNullValue(scope, "scope");
        this.description = description != null ?
                DomainChecks.requireMaxLength(description.trim(), SchemaLimits.SECRET_DESCRIPTION_MAX_LENGTH, "description") :
                null;
    }

    public static Secret create(Long organizationId, String name, SecretScope scope, String description) {
        Instant now = Instant.now();
        return new Secret(null, now, now, null, UUID.randomUUID(), organizationId, name, scope, description);
    }

    public static Secret reconstitute(Long id, UUID secretUuid, Long organizationId,
                                      String name, SecretScope scope, String description,
                                      Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Secret(id, createdAt, updatedAt, deletedAt, secretUuid, organizationId, name, scope, description);
    }

    public void updateName(String newName) {
        this.name = DomainChecks.requireMaxLength(
                DomainChecks.requireText(newName, "name"),
                SchemaLimits.SECRET_NAME_MAX_LENGTH, "name");
        touch();
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription != null ?
                DomainChecks.requireMaxLength(newDescription.trim(), SchemaLimits.SECRET_DESCRIPTION_MAX_LENGTH, "description") :
                null;
        touch();
    }

    /** Публичный метод для обновления updatedAt */
    public void updateTimestamp() {
        touch();
    }
}