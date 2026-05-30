package com.cromp.jobs.domain.model.support;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractAuditableDomainEntityTest {

    @Test
    void shouldMarkDeleteRestoreAndTouchTimestamps() {
        TestEntity entity = new TestEntity();
        pauseBriefly();
        Instant originalUpdatedAt = entity.getUpdatedAt();

        entity.markDeleted();
        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotEqualTo(originalUpdatedAt);

        pauseBriefly();
        Instant beforeRestore = entity.getUpdatedAt();
        entity.restore();
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNotEqualTo(beforeRestore);
    }

    private static final class TestEntity extends AbstractAuditableDomainEntity {
        private TestEntity() {
            super(1L, Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
        }
    }

    private static void pauseBriefly() {
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }
}
