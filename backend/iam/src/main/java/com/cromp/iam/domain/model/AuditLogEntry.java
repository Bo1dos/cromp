package com.cromp.iam.domain.model;

import com.cromp.iam.domain.model.support.SchemaLimits;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

import static com.cromp.iam.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditLogEntry {

    private Long id;

    @EqualsAndHashCode.Include
    private Long organizationId;

    private Instant recordedAt;
    private Long actorId;
    private Map<String, Object> actorSnapshot;
    private String action;
    private String resourceType;
    private Long resourceId;
    private Map<String, Object> changesDiff;

    private AuditLogEntry(Long id,
                          Long organizationId,
                          Instant recordedAt,
                          Long actorId,
                          Map<String, Object> actorSnapshot,
                          String action,
                          String resourceType,
                          Long resourceId,
                          Map<String, Object> changesDiff) {
        this.id = id;
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        this.recordedAt = recordedAt == null ? Instant.now() : recordedAt;
        this.actorId = actorId;
        this.actorSnapshot = safeMap(actorSnapshot);
        this.action = requireMaxLength(
                requireText(action, "action"),
                SchemaLimits.ACTION_MAX_LENGTH,
                "action"
        );
        this.resourceType = normalizeResourceType(resourceType);
        this.resourceId = resourceId;
        this.changesDiff = safeMap(changesDiff);
    }

    private static String normalizeResourceType(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return requireMaxLength(trimmed, SchemaLimits.RESOURCE_TYPE_MAX_LENGTH, "resourceType");
    }

    public static AuditLogEntry record(Long organizationId,
                                       Long actorId,
                                       Map<String, Object> actorSnapshot,
                                       String action,
                                       String resourceType,
                                       Long resourceId,
                                       Map<String, Object> changesDiff) {
        return new AuditLogEntry(
                null,
                organizationId,
                Instant.now(),
                actorId,
                actorSnapshot,
                action,
                resourceType,
                resourceId,
                changesDiff
        );
    }

    public static AuditLogEntry reconstitute(Long id,
                                             Long organizationId,
                                             Instant recordedAt,
                                             Long actorId,
                                             Map<String, Object> actorSnapshot,
                                             String action,
                                             String resourceType,
                                             Long resourceId,
                                             Map<String, Object> changesDiff) {
        return new AuditLogEntry(
                id,
                organizationId,
                recordedAt,
                actorId,
                actorSnapshot,
                action,
                resourceType,
                resourceId,
                changesDiff
        );
    }

    public void validate() {
        requireNonNullValue(organizationId, "organizationId");
        requireNonNullValue(recordedAt, "recordedAt");
        requireText(action, "action");
    }
}