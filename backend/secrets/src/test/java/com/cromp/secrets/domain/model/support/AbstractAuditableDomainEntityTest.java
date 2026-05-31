package com.cromp.secrets.domain.model.support;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractAuditableDomainEntityTest {

    @Test
    void shouldMarkDeletedAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        TestEntity entity = new TestEntity(before, before, null);

        entity.markDeleted();

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isAfter(before);
        assertThat(entity.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldRestoreAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        TestEntity entity = new TestEntity(before, before, Instant.parse("2024-01-01T01:00:00Z"));

        entity.restore();

        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldTouchUpdatedAtWhenTouchIsCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        TestEntity entity = new TestEntity(before, before, null);

        entity.touchNow();

        assertThat(entity.getUpdatedAt()).isAfter(before);
    }

    private static final class TestEntity extends AbstractAuditableDomainEntity {
        private TestEntity(Instant createdAt, Instant updatedAt, Instant deletedAt) {
            super(1L, createdAt, updatedAt, deletedAt);
        }

        private void touchNow() {
            touch();
        }
    }
}
