package com.cromp.iam.domain.model;

import com.cromp.iam.domain.model.support.AbstractAuditableDomainEntity;
import com.cromp.iam.domain.model.support.SchemaLimits;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.cromp.iam.domain.model.support.DomainChecks.requireMaxLength;
import static com.cromp.iam.domain.model.support.DomainChecks.requireNonNullValue;
import static com.cromp.iam.domain.model.support.DomainChecks.requireText;
import static com.cromp.iam.domain.model.support.DomainChecks.safeMap;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Organization extends AbstractAuditableDomainEntity {

    @EqualsAndHashCode.Include
    private UUID orgUuid;

    private String name;

    private Map<String, Object> settings;

    private Organization(Long id,
                         Instant createdAt,
                         Instant updatedAt,
                         Instant deletedAt,
                         UUID orgUuid,
                         String name,
                         Map<String, Object> settings) {
        super(id, createdAt, updatedAt, deletedAt);
        this.orgUuid = orgUuid == null ? UUID.randomUUID() : orgUuid;
        this.name = requireMaxLength(requireText(name, "name"), SchemaLimits.ORG_NAME_MAX_LENGTH, "name");
        this.settings = safeMap(settings);
    }

    public static Organization create(String name) {
        return create(name, Map.of());
    }

    public static Organization create(String name, Map<String, Object> settings) {
        return new Organization(
                null,
                Instant.now(),
                Instant.now(),
                null,
                UUID.randomUUID(),
                name,
                settings
        );
    }

    public static Organization reconstitute(Long id,
                                            UUID orgUuid,
                                            String name,
                                            Map<String, Object> settings,
                                            Instant createdAt,
                                            Instant updatedAt,
                                            Instant deletedAt) {
        return new Organization(
                id,
                createdAt,
                updatedAt,
                deletedAt,
                orgUuid,
                name,
                settings
        );
    }

    public void rename(String name) {
        this.name = requireMaxLength(requireText(name, "name"), SchemaLimits.ORG_NAME_MAX_LENGTH, "name");
        touch();
    }

    public void updateSettings(Map<String, Object> settings) {
        this.settings = safeMap(settings);
        touch();
    }

    public void validate() {
        requireNonNullValue(orgUuid, "orgUuid");
        requireText(name, "name");
    }
}