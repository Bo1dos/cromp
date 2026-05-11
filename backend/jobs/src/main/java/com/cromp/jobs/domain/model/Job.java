package com.cromp.jobs.domain.model;

import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.model.support.AbstractAuditableDomainEntity;
import com.cromp.jobs.domain.model.support.SchemaLimits;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.cromp.jobs.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Job extends AbstractAuditableDomainEntity {

    @EqualsAndHashCode.Include
    private UUID jobUuid;
    private Long organizationId;
    private String name;
    private String description;
    private JobStatus status;
    private String queueName;
    private int priority;
    private Long createdBy;

    private Job(Long id, Instant createdAt, Instant updatedAt, Instant deletedAt,
                UUID jobUuid, Long organizationId, String name, String description,
                JobStatus status, String queueName, int priority, Long createdBy) {
        super(id, createdAt, updatedAt, deletedAt);
        this.jobUuid = jobUuid == null ? UUID.randomUUID() : jobUuid;
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        this.name = requireMaxLength(requireText(name, "name"), SchemaLimits.JOB_NAME_MAX_LENGTH, "name");
        this.description = description != null ? description.trim() : null;
        this.status = requireNonNullValue(status, "status");
        this.queueName = requireMaxLength(requireText(queueName, "queueName"), SchemaLimits.QUEUE_NAME_MAX_LENGTH, "queueName");
        this.priority = priority;
        this.createdBy = createdBy;
    }

    public static Job create(UUID jobUuid, Long organizationId, String name, String description,
                             String queueName, int priority, Long createdBy) {
        Instant now = Instant.now();
        return new Job(null, now, now, null, jobUuid, organizationId, name, description,
                JobStatus.ACTIVE, queueName, priority, createdBy);
    }

    public static Job reconstitute(Long id, UUID jobUuid, Long organizationId, String name,
                                   String description, JobStatus status, String queueName, int priority,
                                   Long createdBy, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Job(id, createdAt, updatedAt, deletedAt, jobUuid, organizationId, name, description,
                status, queueName, priority, createdBy);
    }

    // Поведенческие методы
    public void disable() {
        ensureNotArchived();
        this.status = JobStatus.DISABLED;
        touch();
    }

    public void activate() {
        ensureNotArchived();
        this.status = JobStatus.ACTIVE;
        touch();
    }

    public void archive() {
        this.status = JobStatus.ARCHIVED;
        markDeleted();
    }

    public void rename(String newName) {
        ensureNotArchived();
        this.name = requireMaxLength(requireText(newName, "name"), SchemaLimits.JOB_NAME_MAX_LENGTH, "name");
        touch();
    }

    public void changeDescription(String description) {
        ensureNotArchived();
        this.description = description != null ? description.trim() : null;
        touch();
    }

    public void changeQueue(String queueName) {
        ensureNotArchived();
        this.queueName = requireMaxLength(requireText(queueName, "queueName"), SchemaLimits.QUEUE_NAME_MAX_LENGTH, "queueName");
        touch();
    }

    public void changePriority(int priority) {
        ensureNotArchived();
        this.priority = priority;
        touch();
    }

    private void ensureNotArchived() {
        if (status == JobStatus.ARCHIVED) throw new IllegalStateException("Cannot modify archived job");
    }
}