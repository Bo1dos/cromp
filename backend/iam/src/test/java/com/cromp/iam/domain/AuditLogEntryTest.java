package com.cromp.iam.domain;

import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.domain.model.support.SchemaLimits;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogEntryTest {

    @Test
    void shouldNormalizeResourceTypeAndDefaultMapsWhenRecording() {
        AuditLogEntry entry = AuditLogEntry.record(
                1L,
                2L,
                null,
                "org:update",
                "  organization  ",
                3L,
                null
        );

        assertThat(entry.getRecordedAt()).isNotNull();
        assertThat(entry.getActorSnapshot()).isEmpty();
        assertThat(entry.getChangesDiff()).isEmpty();
        assertThat(entry.getResourceType()).isEqualTo("organization");
    }

    @Test
    void shouldConvertBlankResourceTypeToNull() {
        AuditLogEntry entry = AuditLogEntry.record(
                1L,
                2L,
                Map.of("email", "user@example.com"),
                "user:update",
                "   ",
                3L,
                Map.of("field", "displayName")
        );

        assertThat(entry.getResourceType()).isNull();
        assertThat(entry.getActorSnapshot()).containsEntry("email", "user@example.com");
    }

    @Test
    void shouldRejectBlankActionAndTooLongResourceType() {
        assertThatThrownBy(() -> AuditLogEntry.record(1L, 2L, Map.of(), " ", "user", 3L, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("action");

        assertThatThrownBy(() -> AuditLogEntry.record(
                1L,
                2L,
                Map.of(),
                "user:update",
                "a".repeat(SchemaLimits.RESOURCE_TYPE_MAX_LENGTH + 1),
                3L,
                Map.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("resourceType");
    }
}
