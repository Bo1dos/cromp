package com.cromp.common.event;

import com.cromp.common.event.domain.job.JobExecutionFailedEvent;
import com.cromp.common.event.domain.job.JobExecutionSucceededEvent;
import com.cromp.common.event.domain.job.JobExecutionTimeoutEvent;
import com.cromp.common.event.domain.job.JobDisabledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain Events")
class DomainEventTest {

    @Test
    @DisplayName("JobExecutionSucceededEvent should carry all fields")
    void succeededEventShouldCarryAllFields() {
        UUID jobUuid = UUID.randomUUID();
        UUID execUuid = UUID.randomUUID();
        Long orgId = 42L;
        long durationMs = 1500L;

        var event = new JobExecutionSucceededEvent(jobUuid, "test-job", execUuid, orgId, durationMs);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo("job.execution.succeeded");
        assertThat(event.organizationId()).isEqualTo(orgId);
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.jobUuid()).isEqualTo(jobUuid);
        assertThat(event.jobName()).isEqualTo("test-job");
        assertThat(event.executionUuid()).isEqualTo(execUuid);
        assertThat(event.durationMs()).isEqualTo(durationMs);
    }

    @Test
    @DisplayName("JobExecutionFailedEvent should carry error info")
    void failedEventShouldCarryErrorInfo() {
        UUID jobUuid = UUID.randomUUID();
        UUID execUuid = UUID.randomUUID();
        String error = "Connection refused";

        var event = new JobExecutionFailedEvent(jobUuid, "daily-sync", execUuid, 1L, error);

        assertThat(event.eventType()).isEqualTo("job.execution.failed");
        assertThat(event.errorMessage()).isEqualTo(error);
        assertThat(event.jobName()).isEqualTo("daily-sync");
    }

    @Test
    @DisplayName("JobExecutionTimeoutEvent should carry timeout info")
    void timeoutEventShouldCarryTimeoutInfo() {
        UUID jobUuid = UUID.randomUUID();
        UUID execUuid = UUID.randomUUID();
        long timeoutMs = 30000L;

        var event = new JobExecutionTimeoutEvent(jobUuid, "heavy-job", execUuid, 1L, timeoutMs);

        assertThat(event.eventType()).isEqualTo("job.execution.timeout");
        assertThat(event.timeoutMs()).isEqualTo(timeoutMs);
    }

    @Test
    @DisplayName("JobDisabledEvent should carry job info")
    void disabledEventShouldCarryJobInfo() {
        UUID jobUuid = UUID.randomUUID();

        var event = new JobDisabledEvent(jobUuid, "my-job", 1L, 100L);

        assertThat(event.eventType()).isEqualTo("job.disabled");
        assertThat(event.jobUuid()).isEqualTo(jobUuid);
        assertThat(event.jobName()).isEqualTo("my-job");
        assertThat(event.disabledByUserId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("eventId should default to random UUID when not provided")
    void eventIdShouldDefaultToRandom() {
        var event = new JobExecutionSucceededEvent(UUID.randomUUID(), "j", UUID.randomUUID(), 1L, 100L);

        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }
}
