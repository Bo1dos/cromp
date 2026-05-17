package com.cromp.jobs.domain.model;

import com.cromp.jobs.domain.model.support.DomainChecks;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JobVersion {

    @EqualsAndHashCode.Include
    private Long id;
    @EqualsAndHashCode.Include
    private Long jobId;
    @EqualsAndHashCode.Include
    private int version;
    private JobConfig config;
    private Long createdBy;
    private Instant createdAt;

    private JobVersion(Long id, Long jobId, int version, JobConfig config,
                       Long createdBy, Instant createdAt) {
        this.id = id;
        this.jobId = DomainChecks.requireNonNullValue(jobId, "jobId");
        this.version = version;
        this.config = DomainChecks.requireNonNullValue(config, "config");
        this.createdBy = createdBy;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public static JobVersion create(Long jobId, int version, JobConfig config, Long createdBy) {
        return new JobVersion(null, jobId, version, config, createdBy, Instant.now());
    }

    public static JobVersion reconstitute(Long id, Long jobId, int version, JobConfig config,
                                          Long createdBy, Instant createdAt) {
        return new JobVersion(id, jobId, version, config, createdBy, createdAt);
    }
}