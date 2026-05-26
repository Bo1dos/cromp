package com.cromp.iam.domain.model.support;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractAuditableDomainEntity {

    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    protected AbstractAuditableDomainEntity(Long id,
                                            Instant createdAt,
                                            Instant updatedAt,
                                            Instant deletedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
        touch();
    }

    public void restore() {
        this.deletedAt = null;
        touch();
    }

    protected void touch() {
        this.updatedAt = Instant.now();
    }
}