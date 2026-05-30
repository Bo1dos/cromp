package com.cromp.jobs.domain.model;

import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.model.exceptions.InvalidJobStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobTest {

    @Test
    void shouldCreateActiveJobWithGeneratedUuidWhenUuidIsNull() {
        Job job = Job.create(null, 11L, "Daily sync", null, "default", 5, 42L);

        assertThat(job.getJobUuid()).isNotNull();
        assertThat(job.getOrganizationId()).isEqualTo(11L);
        assertThat(job.getName()).isEqualTo("Daily sync");
        assertThat(job.getDescription()).isNull();
        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.getCreatedBy()).isEqualTo(42L);
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotNull();
        assertThat(job.getCreatedAt()).isEqualTo(job.getUpdatedAt());
    }

    @Test
    void shouldReconstituteJobWithStableIdentity() {
        UUID jobUuid = UUID.randomUUID();
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-01-01T01:00:00Z");

        Job job = Job.reconstitute(1L, jobUuid, 11L, "Daily sync", "desc", JobStatus.DISABLED,
                "default", 5, 42L, createdAt, updatedAt, null);

        assertThat(job.getId()).isEqualTo(1L);
        assertThat(job.getJobUuid()).isEqualTo(jobUuid);
        assertThat(job.getCreatedAt()).isEqualTo(createdAt);
        assertThat(job.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldTrimDescriptionAndPreserveImmutableIdentifierAcrossUpdates() {
        Job job = Job.create(UUID.randomUUID(), 11L, "Daily sync", "  initial  ", "default", 5, 42L);
        UUID originalUuid = job.getJobUuid();

        job.rename("  Daily sync v2  ");
        job.changeDescription("  updated  ");
        job.changeQueue("  priority  ");
        job.changePriority(7);

        assertThat(job.getJobUuid()).isEqualTo(originalUuid);
        assertThat(job.getName()).isEqualTo("Daily sync v2");
        assertThat(job.getDescription()).isEqualTo("updated");
        assertThat(job.getQueueName()).isEqualTo("priority");
        assertThat(job.getPriority()).isEqualTo(7);
    }

    @Test
    void shouldThrowWhenNameIsBlankOrTooLong() {
        assertThatThrownBy(() -> Job.create(UUID.randomUUID(), 11L, "   ", null, "default", 1, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be blank");

        assertThatThrownBy(() -> Job.create(UUID.randomUUID(), 11L, "x".repeat(256), null, "default", 1, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not exceed 255 characters");
    }

    @Test
    void shouldThrowWhenOrganizationIdIsNull() {
        assertThatThrownBy(() -> Job.create(UUID.randomUUID(), null, "Daily sync", null, "default", 1, 42L))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("organizationId must not be null");
    }

    @Test
    void shouldThrowWhenQueueNameIsInvalid() {
        assertThatThrownBy(() -> Job.create(UUID.randomUUID(), 11L, "Daily sync", null, "   ", 1, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("queueName must not be blank");
    }

    @Test
    void shouldDisableEnableArchiveAndRestoreJob() {
        Job job = Job.create(UUID.randomUUID(), 11L, "Daily sync", null, "default", 5, 42L);
        pauseBriefly();
        Instant beforeDisable = job.getUpdatedAt();

        job.disable();
        assertThat(job.getStatus()).isEqualTo(JobStatus.DISABLED);
        assertThat(job.getUpdatedAt()).isNotEqualTo(beforeDisable);

        pauseBriefly();
        Instant beforeActivate = job.getUpdatedAt();
        job.activate();
        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.getUpdatedAt()).isNotEqualTo(beforeActivate);

        pauseBriefly();
        Instant beforeArchive = job.getUpdatedAt();
        job.archive();
        assertThat(job.getStatus()).isEqualTo(JobStatus.ARCHIVED);
        assertThat(job.isDeleted()).isTrue();
        assertThat(job.getDeletedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotEqualTo(beforeArchive);

        job.restore();
        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.isDeleted()).isFalse();
    }

    @Test
    void shouldRejectDuplicateEnableDisableAndInvalidRestore() {
        Job active = Job.create(UUID.randomUUID(), 11L, "Daily sync", null, "default", 5, 42L);
        assertThatThrownBy(active::activate)
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessage("Job is already active");

        active.disable();
        assertThatThrownBy(active::disable)
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessage("Job is already disabled");

        Job another = Job.create(UUID.randomUUID(), 11L, "Daily sync", null, "default", 5, 42L);
        assertThatThrownBy(another::restore)
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessage("Job is not archived");
    }

    @Test
    void shouldRejectArchivedMutations() {
        Job job = Job.create(UUID.randomUUID(), 11L, "Daily sync", null, "default", 5, 42L);
        job.archive();

        assertThatThrownBy(() -> job.rename("next"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify archived job");
        assertThatThrownBy(() -> job.changeDescription("next"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify archived job");
        assertThatThrownBy(() -> job.changeQueue("next"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify archived job");
        assertThatThrownBy(() -> job.changePriority(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify archived job");
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
